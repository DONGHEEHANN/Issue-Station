package com.issuestation.Controller;

import com.issuestation.Dto.Issue.IssueRequestDto;
import com.issuestation.Dto.Issue.IssueResponseDto;
import com.issuestation.Entity.Assignee;
import com.issuestation.Entity.Comment;
import com.issuestation.Entity.Issue;
import com.issuestation.Entity.Reporter;
import com.issuestation.Entity.enums.Status;
import com.issuestation.Security.TokenProvider;
import com.issuestation.Service.IssueService.*;
import com.issuestation.apiPayload.ApiResponse;
import com.issuestation.apiPayload.code.status.ErrorStatus;
import com.issuestation.apiPayload.exception.handler.TempHandler;
import com.issuestation.converter.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/issue")
public class IssueController {

    private final IssueInfoServiceImpl issueInfoService;
    private final IssueCreateService issueCreateService;
    private final IssueDeleteService issueDeleteService;
    private final IssueStateService issueStateService;
    private final CommentCreateService commentCreateService;
    private final IssueSearchService issueSearchService;
    private final SetAssigneeService assigneeService;
    @Autowired
    TokenProvider tokenProvider;

    private long checkToken(HttpServletRequest token) {
        var getToken = token.getHeader("Authorization");
        String jwtToken = getToken.replace("Bearer ", "");
        long loginId;
        try {
            loginId = Long.parseLong(tokenProvider.validateJwt(jwtToken));
            System.out.println("user id: " + loginId);
        } catch (Exception e) {
            throw new TempHandler(ErrorStatus._UNAUTHORIZED);
        }
        return loginId;
    }

    @PostMapping("/create/{id}")
    public ApiResponse<IssueResponseDto.JoinIssueCreateResponseDto> join(HttpServletRequest token, @PathVariable("id") long projectId, @RequestBody @Valid IssueRequestDto.JoinIssueCreateRequestDto request) {

        //토큰 검증
        checkToken(token);

        Issue issue = issueCreateService.joinIssue(request, projectId);
        return ApiResponse.onSuccess(IssueCreateConverter.toIssueDto(issue));
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<IssueResponseDto.JoinIssueDeleteResponseDto> join(HttpServletRequest token, @PathVariable("id") long issueId) {

        // 토큰 검증
        checkToken(token);

        issueDeleteService.deleteIssue(issueId);
        return ApiResponse.onSuccess(new IssueResponseDto.JoinIssueDeleteResponseDto(issueId));
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<List<IssueResponseDto.JoinIssueSearchResponseDto>> searchIssues(HttpServletRequest token,
                                                                                          @PathVariable("id") long projectId,
                                                                                          @RequestParam(required = false) String name,
                                                                                          @RequestParam(required = false) Status status) {
        // 토큰 검증
        checkToken(token);

        List<IssueResponseDto.JoinIssueSearchResponseDto> issues;
        if (name != null && status != null) {
            issues = issueSearchService.searchIssuesByProjectIdNameAndStatus(projectId, name, status);
        } else if (name == null && status == null) {
            issues = issueSearchService.searchIssuesByProjectId(projectId);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (issues.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(issues, HttpStatus.OK);
    }

    @GetMapping("/info/{id}")
    public ApiResponse<IssueResponseDto.JoinIssueInfoResponseDto> info(HttpServletRequest token, @PathVariable long id) {
//        checkToken(token);
        return ApiResponse.onSuccess(issueInfoService.getIssueDetails(id));
    }

    @PostMapping("/state/{id}")
    public ApiResponse<IssueResponseDto.JoinIssueStateResponseDto> changeState(HttpServletRequest token, @PathVariable("id") long issueId, @RequestBody @Valid IssueRequestDto.JoinIssueStateRequestDto request) {

        //토큰 검증
        checkToken(token);

        Issue issue = issueStateService.changeIssueState(request, issueId);
        return ApiResponse.onSuccess(IssueStateConverter.toIssueDto(issue));
    }

    @PostMapping("/comment/create/{id}")
    public ApiResponse<IssueResponseDto.JoinCommentCreateResponseDto> createComment(HttpServletRequest token, @PathVariable long id, @RequestBody @Valid IssueRequestDto.JoinCommentCreateRequestDto request) {
        checkToken(token);
        var getToken = token.getHeader("Authorization");
        String jwtToken = getToken.replace("Bearer ", "");
        Comment comment = commentCreateService.createComment(request, id, jwtToken);
        return ApiResponse.onSuccess(CommentCreateConverter.toCommentDto(comment));
    }

    @PostMapping("/assignee/{id}")
    public ApiResponse<IssueResponseDto.JoinAssigneeCreateResponseDto> assignAssigneeToIssue(@PathVariable long id, @RequestBody IssueRequestDto.JoinAssigneeRequestDto request) {
        Assignee assignee = assigneeService.assignAssignee(id, request);
        return ApiResponse.onSuccess(JoinAssigneeConverter.toIssueDto(assignee));
    }

    @PostMapping("/reporter/{id}")
    public ApiResponse<IssueResponseDto.JoinAssigneeCreateResponseDto> assignReporterToIssue(@PathVariable long id, @RequestBody IssueRequestDto.JoinAssigneeRequestDto request) {
        Reporter reporter = assigneeService.assignReporter(id, request);
        return ApiResponse.onSuccess(JoinAssigneeConverter.toReporterDto(reporter));
    }
}