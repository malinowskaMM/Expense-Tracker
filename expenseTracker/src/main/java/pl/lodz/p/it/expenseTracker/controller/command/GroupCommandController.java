package pl.lodz.p.it.expenseTracker.controller.command;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.expenseTracker.dto.group.request.GroupChangeHeadRequestDto;
import pl.lodz.p.it.expenseTracker.dto.group.request.GroupChangeRequestDto;
import pl.lodz.p.it.expenseTracker.dto.group.request.GroupCreateRequestDto;
import pl.lodz.p.it.expenseTracker.service.GroupCommandService;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@CrossOrigin
@SecurityRequirement(name = "Authorization")
public class GroupCommandController {

    private final GroupCommandService service;

    @PatchMapping("/group/{id}/head")
    public ResponseEntity<Void> changeHeadOfGroup(@RequestBody @Valid GroupChangeHeadRequestDto request, @PathVariable String id,
                                                  @RequestHeader(value = "If-Match", required = true) String ifMatchHeader) {
        return ResponseEntity.ok(service.changeHeadOfGroup(id, request.getNewOwnerIds(), request.getVersion(), ifMatchHeader));
    }

    @PostMapping("/group")
    public ResponseEntity<Void> createGroup(@RequestBody @Valid GroupCreateRequestDto request) {
        return ResponseEntity.ok(service.createGroup(request.getName(),request.getAccountsIds(), request.getOwnerId()));
    }

    @PatchMapping("/group/{id}")
    public ResponseEntity<Void> changeGroup(@RequestBody @Valid GroupChangeRequestDto request, @PathVariable String id,
                                            @RequestHeader(value = "If-Match", required = false) String ifMatchHeader) {
        return ResponseEntity.ok(service.changeGroup(id, request.getAccountId(), request.getGroupName(),
                request.getAccountsEmails(), request.getVersion(), ifMatchHeader));
    }

    @PatchMapping("/group/{id}/leave")
    public ResponseEntity<Void> leaveGroup(@RequestBody @Valid String accountId, @PathVariable String id) {
        service.leaveGroup(id, accountId);
        return ResponseEntity.ok().build();
    }
}