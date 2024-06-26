package com.found_404.funco.note.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.found_404.funco.feignClient.dto.NotificationType;
import com.found_404.funco.feignClient.dto.SimpleMember;
import com.found_404.funco.feignClient.service.MemberService;
import com.found_404.funco.feignClient.service.NotificationService;
import com.found_404.funco.note.domain.Note;
import com.found_404.funco.note.domain.NoteComment;
import com.found_404.funco.note.domain.NoteLike;
import com.found_404.funco.note.domain.repository.NoteCommentRepository;
import com.found_404.funco.note.domain.repository.NoteLikeRepository;
import com.found_404.funco.note.domain.repository.NoteRepository;
import com.found_404.funco.note.dto.request.CommentRequest;
import com.found_404.funco.note.dto.request.NoteRequest;
import com.found_404.funco.note.dto.request.NotesFilterRequest;
import com.found_404.funco.note.dto.response.*;
import com.found_404.funco.note.dto.type.PostType;
import com.found_404.funco.note.exception.NoteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.found_404.funco.note.exception.NoteErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoteService {

    private final static int THUMBNAIL_CONTENT_LENGTH = 120;

    private final NoteRepository noteRepository;
    private final NoteCommentRepository noteCommentRepository;
    private final NoteLikeRepository noteLikeRepository;

    private final MemberService memberService;
    private final NotificationService notificationService;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public ImageResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new NoteException(NOT_FOUND_IMAGE);
        }

        String originName = file.getOriginalFilename();
        String type = Objects.requireNonNull(originName).substring(originName.lastIndexOf(".") + 1);

        validationFileType(type.toUpperCase());

        String fileName = UUID.randomUUID() + originName;
        ObjectMetadata objectMetadata = new ObjectMetadata();

        try {
            objectMetadata.setContentLength(file.getInputStream().available());
            amazonS3.putObject(bucketName, fileName, file.getInputStream(), objectMetadata);
        } catch (IOException e) {
            throw new NoteException(IMAGE_UPLOAD_FAIL);
        }

        return new ImageResponse(amazonS3.getUrl(bucketName, fileName).toString());
    }

    private void validationFileType(String type) {
        switch (type) {
            case "PNG", "JPG", "JPEG", "GIF" -> {}
            default -> throw new NoteException(MISS_MATCH_IMAGE_TYPE);
        }
    }


    public List<NotesResponse> getNotes(Long memberId, NotesFilterRequest notesFilterRequest, Pageable pageable) {
        if ((notesFilterRequest.type() == PostType.MY || notesFilterRequest.type() == PostType.LIKE)
                && Objects.isNull(memberId)) {
            throw new NoteException(INVALID_FILTER);
        }
        List<Note> notes = noteRepository.getNotesWithFilter(memberId ,notesFilterRequest, pageable);
        Map<Long, SimpleMember> simpleMembers = memberService.getSimpleMember(notes.stream().map(Note::getMemberId).toList());

        return notes
            .stream().map(note ->  NotesResponse.builder()
                .noteId(note.getId())
                .member(simpleMembers.get(note.getMemberId()))
                .thumbnailImage(note.getThumbnailImage())
                .thumbnailContent(note.getThumbnailContent())
                .title(note.getTitle())
                .ticker(note.getTicker())
                .writeDate(note.getCreatedAt())
                .likeCount(noteLikeRepository.countByNote(note))
                .liked(Objects.nonNull(memberId) && noteLikeRepository.existsByMemberIdAndNoteId(
                    memberId, note.getId()))
                .commentCount(noteCommentRepository.countByNote(note))
                .build())
            .toList();

    }

    public NoteResponse getNote(Long memberId, Long noteId) {
        Note note = getNote(noteId);
        Long likeCount = noteLikeRepository.countByNote(note);

        Map<Long, SimpleMember> simpleMembers = memberService.getSimpleMember(note.getMemberId());

        return NoteResponse.builder()
                .noteId(note.getId())
                .member(simpleMembers.get(note.getMemberId()))
                .title(note.getTitle())
                .content(note.getContent())
                .ticker(note.getTicker())
                .likeCount(likeCount)
                .liked(Objects.nonNull(memberId) && noteLikeRepository.existsByMemberIdAndNoteId(
                    memberId, note.getId()))
                .writeDate(note.getCreatedAt())
                .build();
    }

    private Note getNote(Long noteId) {
        return noteRepository.findById(noteId).orElseThrow(() -> new NoteException(NOT_FOUND_NOTE));
    }


    public AddNoteResponse addNote(Long memberId, NoteRequest request) {
        Note note = noteRepository.save(Note.builder()
                .memberId(memberId)
                .title(request.title())
                .content(request.content())
                .ticker(request.ticker())
                .thumbnailImage(request.thumbnailImage())
                .thumbnailContent(getThumbnailContent(request.content(), THUMBNAIL_CONTENT_LENGTH))
                .build());

        return AddNoteResponse.builder()
            .noteId(note.getId())
            .build();
    }

    @Transactional
    public void removeNote(Long memberId, Long noteId) {
        checkAuthorization(memberId, getNote(noteId));

        if (!noteRepository.deleteNoteWithComments(noteId)) {
            throw new NoteException(DELETE_FAIL);
        }
    }

    private static void checkAuthorization(Long memberId, Note note) {
        if (!note.getMemberId().equals(memberId)) {
           throw new NoteException(UNAUTHORIZED);
        }
    }

    @Transactional
    public void editNote(Long memberId, Long noteId, NoteRequest request) {
        Note note = getNote(noteId);
        checkAuthorization(memberId, note);
        note.editNote(request.title(), request.content(), request.ticker(), request.thumbnailImage(), getThumbnailContent(request.content(), THUMBNAIL_CONTENT_LENGTH));
    }

    public NoteCommentResponse getComments(Long noteId) {
        List<NoteComment> comments = noteCommentRepository.findByNoteId(noteId);

        Map<Long, SimpleMember> simpleMembers = memberService.getSimpleMember(comments.stream()
                .map(NoteComment::getMemberId)
                .collect(Collectors.toSet())
                .stream().toList());

        List<CommentsResponse> commentsResponses = new ArrayList<>();
        Map<Long, List<NoteComment>> childComments = new HashMap<>();
        for (NoteComment comment : comments) {
            long key = Objects.isNull(comment.getParentId()) ? 0 : comment.getParentId();
            childComments.putIfAbsent(key, new ArrayList<>());
            childComments.get(key).add(comment);
        }

        for (NoteComment comment : childComments.getOrDefault(0L, Collections.emptyList())) {
            commentsResponses.add(
                getCommentsResponse(childComments, comment, simpleMembers)
            );
        }

        return NoteCommentResponse.builder()
            .comments(commentsResponses)
            .commentCount(comments.size())
            .build();
    }

    private CommentsResponse getCommentsResponse(Map<Long, List<NoteComment>> childComments, NoteComment comment, Map<Long, SimpleMember> simpleMembers) {

        return CommentsResponse.builder()
            .commentId(comment.getId())
            .member(simpleMembers.get(comment.getMemberId()))
            .content(comment.getContent())
            .date(comment.getCreatedAt())
            .childComments(childComments.getOrDefault(comment.getId(), Collections.emptyList())
                .stream().map(childComment -> getCommentsResponse(childComments, childComment, simpleMembers))
                .toList())
            .build();
    }

    public void addComment(Long memberId, Long noteId, CommentRequest request) {
        Note note = getNote(noteId);

        noteCommentRepository.save(NoteComment.builder()
                .memberId(memberId)
                .note(note)
                .parentId(request.parentCommentId())
                .content(request.content())
                .build());

        // [API 알림]
        notificationService.sendNotification(note.getMemberId(), NotificationType.NOTE_COMMENT, note.getTitle().substring(0, Math.min(20, note.getTitle().length())) + "... 게시글에 댓글이 달렸어요.");
        if (Objects.nonNull(request.parentCommentId())) {
            noteCommentRepository.findById(request.parentCommentId())
                    .ifPresent(noteComment -> notificationService.sendNotification(noteComment.getMemberId(), NotificationType.COMMENT_ANSWER,
                            note.getTitle().substring(0, Math.min(20, note.getTitle().length())) + "... 게시글에 내 댓글에 답글이 달렸어요."));
        }
    }

    public String getThumbnailContent(String content, int length) {
        Document doc = Jsoup.parse(content);
        doc.select("img").remove();

        return doc.text().substring(0, Math.min(doc.text().length(), length));
    }

    public void addNoteLike(Long memberId, Long noteId) {
        Note note = getNote(noteId);

        Optional<NoteLike> noteLike = noteLikeRepository.findByMemberIdAndNote(memberId, note);

        if (noteLike.isPresent()) {
            noteLikeRepository.delete(noteLike.get());
        }
        else {
            noteLikeRepository.save(NoteLike.builder()
                    .memberId(memberId)
                    .note(note)
                    .build());
        }

    }





}
