package com.example.capstone3.Config;

import com.example.capstone3.Models.*;
import com.example.capstone3.Repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

/** Deny-by-default ownership rules for every legacy API route. */
@Component
@RequiredArgsConstructor
public class ApiAuthorizationInterceptor implements HandlerInterceptor {
    private final ApartmentRepository apartments;
    private final BuildingRepository buildings;
    private final UserPreferenceRepository preferences;
    private final ReservationRepository reservations;
    private final ContractRepository contracts;
    private final MaintenanceRepository maintenance;
    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final ReviewRepository reviews;
    private final RoommateRequestRepository roommateRequests;

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if ("OPTIONS".equals(method)) return true;
        String path = request.getRequestURI().substring("/api/v1/".length());
        if (path.startsWith("auth/")) return true;
        if (isPublic(method, path)) return true;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal actor)) deny();
        AuthPrincipal actor = (AuthPrincipal) authentication.getPrincipal();
        Map<String, String> vars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (vars == null) deny();
        String[] parts = path.split("/");
        String area = parts[0];
        String action = parts.length > 1 ? parts[1] : "";
        boolean allowed = switch (area) {
            case "user" -> own(actor, "USER", id(vars, "id"));
            case "owner" -> own(actor, "OWNER", id(vars, "id"));
            case "building" -> buildingRule(actor, action, vars);
            case "apartment" -> apartmentRule(actor, action, vars);
            case "user-preference", "user-preferences" -> preferenceRule(actor, action, vars);
            case "recommendation" -> own(actor, "USER", id(vars, "userId"));
            case "reservation" -> reservationRule(actor, action, parts, vars);
            case "contract" -> contractRule(actor, action, parts, vars);
            case "maintenance-request" -> maintenanceRule(actor, action, vars);
            case "conversation" -> conversationRule(actor, action, vars);
            case "message" -> messageRule(actor, action, parts, vars, request);
            case "reviews" -> reviewRule(actor, action, parts, vars);
            case "roommates" -> roommateRule(actor, action, vars);
            case "ai" -> aiRule(actor, parts, vars);
            case "location" -> "analyze".equals(action);
            default -> false;
        };
        if (!allowed) deny();
        return true;
    }

    private boolean isPublic(String method, String path) {
        return "GET".equals(method) && (
                path.equals("building/get") || path.matches("building/get/\\d+") ||
                path.equals("apartment/get") || path.matches("apartment/get/\\d+") ||
                path.equals("apartment/search") || path.equals("apartment/get/underpriced"));
    }

    private boolean buildingRule(AuthPrincipal actor, String action, Map<String, String> v) {
        return switch (action) {
            case "add" -> own(actor, "OWNER", id(v, "owner_id"));
            case "update", "delete" -> ownsBuilding(actor, id(v, "id"));
            default -> false;
        };
    }

    private boolean apartmentRule(AuthPrincipal actor, String action, Map<String, String> v) {
        return switch (action) {
            case "add" -> own(actor, "OWNER", id(v, "ownerId")) && ownsBuilding(actor, id(v, "buildingId"));
            case "update", "delete" -> ownsApartment(actor, id(v, "id"));
            case "low-rated" -> own(actor, "OWNER", id(v, "ownerId")) && ownsBuilding(actor, id(v, "buildingId"));
            case "dashboard", "flagged" -> own(actor, "OWNER", id(v, "ownerId"));
            case "available", "toggle-maintenance" -> own(actor, "OWNER", id(v, "ownerId")) && ownsApartment(actor, id(v, "apartmentId"));
            case "next-available", "available-on" -> true;
            default -> false;
        };
    }

    private boolean preferenceRule(AuthPrincipal actor, String action, Map<String, String> v) {
        return switch (action) {
            case "add", "user", "add-workplace" -> own(actor, "USER", id(v, "userId"));
            case "get", "update", "delete" -> id(v, "id") != null && ownsPreference(actor, id(v, "id"));
            default -> false;
        };
    }

    private boolean reservationRule(AuthPrincipal actor, String action, String[] p, Map<String, String> v) {
        return switch (action) {
            case "get" -> {
                if (v.containsKey("id")) yield reservationParty(actor, id(v, "id"));
                if (v.containsKey("ownerId")) yield own(actor, "OWNER", id(v, "ownerId"));
                if (v.containsKey("userId")) yield own(actor, "USER", id(v, "userId"));
                yield false;
            }
            case "add" -> own(actor, "USER", id(v, "userId"));
            case "update", "delete" -> userReservation(actor, id(v, "id"));
            case "accept", "reject" -> own(actor, "OWNER", id(v, "ownerId")) && ownsReservation(actor, id(v, "reservationId"));
            case "end" -> own(actor, "USER", id(v, "userId")) && userReservation(actor, id(v, "reservationId"));
            default -> false;
        };
    }

    private boolean contractRule(AuthPrincipal actor, String action, String[] p, Map<String, String> v) {
        Integer contractId = id(v, "contractId");
        return switch (action) {
            case "get" -> id(v, "id") != null && contractParty(actor, id(v, "id"));
            case "add" -> own(actor, "OWNER", id(v, "ownerId")) && ownsReservation(actor, id(v, "reservation_id"));
            case "update", "delete" -> ownsContract(actor, id(v, "id"));
            case "user" -> own(actor, "USER", id(v, "userId"));
            case "owner" -> own(actor, "OWNER", id(v, "ownerId"));
            case "accept", "reject", "analyze" -> own(actor, "USER", id(v, "userId")) && userContract(actor, contractId);
            case "end", "terminate" -> own(actor, "OWNER", id(v, "ownerId")) && ownsContract(actor, contractId);
            case "renew" -> {
                if (p.length > 2 && ("approve".equals(p[2]) || "reject".equals(p[2])))
                    yield own(actor, "OWNER", id(v, "ownerId")) && ownsContract(actor, contractId);
                yield own(actor, "USER", id(v, "userId")) && userContract(actor, contractId);
            }
            default -> p.length > 2 && "send-pdf".equals(p[2]) && ownsContract(actor, contractId);
        };
    }

    private boolean maintenanceRule(AuthPrincipal actor, String action, Map<String, String> v) {
        return switch (action) {
            case "get" -> id(v, "id") != null && maintenanceParty(actor, id(v, "id"));
            case "user" -> own(actor, "USER", id(v, "userId"));
            case "apartment" -> ownsApartment(actor, id(v, "apartmentId"));
            case "building-summary" -> ownsBuilding(actor, id(v, "buildingId"));
            case "add" -> own(actor, "USER", id(v, "userId"));
            case "start", "complete" -> own(actor, "OWNER", id(v, "ownerId")) && ownsMaintenance(actor, id(v, "requestId"));
            case "update", "delete" -> own(actor, "USER", id(v, "userId")) && userMaintenance(actor, id(v, "id"));
            default -> false;
        };
    }

    private boolean conversationRule(AuthPrincipal actor, String action, Map<String, String> v) {
        return switch (action) {
            case "get" -> id(v, "id") != null && conversationParty(actor, id(v, "id"));
            case "user" -> own(actor, "USER", id(v, "userId"));
            case "owner" -> own(actor, "OWNER", id(v, "ownerId"));
            default -> false;
        };
    }

    private boolean messageRule(AuthPrincipal actor, String action, String[] p, Map<String, String> v, HttpServletRequest request) {
        return switch (action) {
            case "get" -> id(v, "id") != null && messageParty(actor, id(v, "id"));
            case "conversation" -> conversationParty(actor, id(v, "conversationId"));
            case "add" -> {
                boolean apartmentOwner = apartmentOwnedBy(id(v, "apartmentId"), id(v, "ownerId"));
                if (p.length > 2 && "user".equals(p[2])) yield own(actor, "USER", id(v, "userId")) && apartmentOwner;
                if (p.length > 2 && "owner".equals(p[2])) yield own(actor, "OWNER", id(v, "ownerId")) && apartmentOwner &&
                        conversations.findByUser_IdAndOwner_IdAndApartment_Id(id(v, "userId"), id(v, "ownerId"), id(v, "apartmentId")) != null;
                yield false;
            }
            case "update", "delete" -> {
                Integer messageId = id(v, "id");
                Message message = messageId == null ? null : messages.findById(messageId).orElse(null);
                String senderRole = request.getParameter("senderRole");
                Integer senderId = parse(request.getParameter("senderId"));
                yield message != null && own(actor, senderRole, senderId) &&
                        actor.id().equals(message.getSenderId()) && actor.role().equalsIgnoreCase(message.getSenderRole()) &&
                        conversationParty(actor, message.getConversation().getId());
            }
            default -> false;
        };
    }

    private boolean reviewRule(AuthPrincipal actor, String action, String[] p, Map<String, String> v) {
        return switch (action) {
            case "get" -> {
                if (v.containsKey("userId")) yield own(actor, "USER", id(v, "userId"));
                if (v.containsKey("ownerId")) yield own(actor, "OWNER", id(v, "ownerId"));
                yield v.containsKey("id") || v.containsKey("apartmentId");
            }
            case "add" -> own(actor, "USER", id(v, "userId")) && userReservation(actor, id(v, "reservationId"));
            case "update", "delete" -> own(actor, "USER", id(v, "userId")) && userReview(actor, id(v, "id"));
            default -> false;
        };
    }

    private boolean roommateRule(AuthPrincipal actor, String action, Map<String, String> v) {
        return switch (action) {
            case "requests", "matches", "dissolve" -> own(actor, "USER", id(v, "userId"));
            case "request" -> own(actor, "USER", id(v, "senderId"));
            case "accept", "reject" -> {
                Integer requestId = id(v, "requestId");
                RoommateRequest item = requestId == null ? null : roommateRequests.findById(requestId).orElse(null);
                yield own(actor, "USER", id(v, "receiverId")) && item != null && actor.id().equals(item.getReceiver().getId());
            }
            default -> false;
        };
    }

    private boolean aiRule(AuthPrincipal actor, String[] p, Map<String, String> v) {
        if (p.length > 2 && "owners".equals(p[1])) return own(actor, "OWNER", id(v, "ownerId"));
        return p.length > 2 && "apartments".equals(p[1]);
    }

    private boolean own(AuthPrincipal actor, String role, Integer id) { return id != null && role != null && role.equalsIgnoreCase(actor.role()) && id.equals(actor.id()); }
    private boolean ownsBuilding(AuthPrincipal a, Integer id) { Building b = id == null ? null : buildings.findById(id).orElse(null); return b != null && own(a, "OWNER", b.getOwner().getId()); }
    private boolean ownsApartment(AuthPrincipal a, Integer id) { Apartment x = id == null ? null : apartments.findById(id).orElse(null); return x != null && own(a, "OWNER", x.getOwner().getId()); }
    private boolean apartmentOwnedBy(Integer apartmentId, Integer ownerId) { Apartment x = apartmentId == null ? null : apartments.findById(apartmentId).orElse(null); return x != null && ownerId != null && ownerId.equals(x.getOwner().getId()); }
    private boolean ownsPreference(AuthPrincipal a, Integer id) { UserPreference x = id == null ? null : preferences.findById(id).orElse(null); return x != null && own(a, "USER", x.getUser().getId()); }
    private boolean ownsReservation(AuthPrincipal a, Integer id) { Reservation x = id == null ? null : reservations.findById(id).orElse(null); return x != null && own(a, "OWNER", x.getApartment().getOwner().getId()); }
    private boolean userReservation(AuthPrincipal a, Integer id) { Reservation x = id == null ? null : reservations.findById(id).orElse(null); return x != null && own(a, "USER", x.getUser().getId()); }
    private boolean reservationParty(AuthPrincipal a, Integer id) { return ownsReservation(a, id) || userReservation(a, id); }
    private boolean ownsContract(AuthPrincipal a, Integer id) { Contract x = id == null ? null : contracts.findById(id).orElse(null); return x != null && own(a, "OWNER", x.getReservation().getApartment().getOwner().getId()); }
    private boolean userContract(AuthPrincipal a, Integer id) { Contract x = id == null ? null : contracts.findById(id).orElse(null); return x != null && own(a, "USER", x.getReservation().getUser().getId()); }
    private boolean contractParty(AuthPrincipal a, Integer id) { return ownsContract(a, id) || userContract(a, id); }
    private boolean ownsMaintenance(AuthPrincipal a, Integer id) { MaintenanceRequest x = id == null ? null : maintenance.findById(id).orElse(null); return x != null && own(a, "OWNER", x.getApartment().getOwner().getId()); }
    private boolean userMaintenance(AuthPrincipal a, Integer id) { MaintenanceRequest x = id == null ? null : maintenance.findById(id).orElse(null); return x != null && own(a, "USER", x.getUser().getId()); }
    private boolean maintenanceParty(AuthPrincipal a, Integer id) { return ownsMaintenance(a, id) || userMaintenance(a, id); }
    private boolean conversationParty(AuthPrincipal a, Integer id) { Conversation x = id == null ? null : conversations.findById(id).orElse(null); return x != null && (own(a, "USER", x.getUser().getId()) || own(a, "OWNER", x.getOwner().getId())); }
    private boolean messageParty(AuthPrincipal a, Integer id) { Message x = id == null ? null : messages.findById(id).orElse(null); return x != null && conversationParty(a, x.getConversation().getId()); }
    private boolean userReview(AuthPrincipal a, Integer id) { Review x = id == null ? null : reviews.findById(id).orElse(null); return x != null && own(a, "USER", x.getUser().getId()); }
    private Integer id(Map<String, String> vars, String key) { return parse(vars.get(key)); }
    private Integer parse(String value) { try { return value == null ? null : Integer.valueOf(value); } catch (NumberFormatException e) { return null; } }
    private void deny() { throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"); }
}
