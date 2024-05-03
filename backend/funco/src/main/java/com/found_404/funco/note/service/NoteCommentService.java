package com.found_404.funco.note.service;

import static com.found_404.funco.member.exception.MemberErrorCode.INVALID_MEMBER;
import static com.found_404.funco.note.exception.NoteCommentErrorCode.NOT_FOUND_NOTE_COMMENT;

import com.found_404.funco.member.domain.Member;
import com.found_404.funco.member.exception.MemberException;
import com.found_404.funco.note.domain.NoteComment;
import com.found_404.funco.note.domain.repository.NoteCommentRepository;
import com.found_404.funco.note.dto.request.EditNoteCommentRequest;
import com.found_404.funco.note.exception.NoteCommentException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoteCommentService {
    
    private final NoteCommentRepository noteCommentRepository;

    @Transactional
    public void editComment(Member member, Long commentId, EditNoteCommentRequest request) {
        NoteComment comment = noteCommentRepository.findById(commentId).orElseThrow(() -> new NoteCommentException(NOT_FOUND_NOTE_COMMENT));
        if (Objects.isNull(member) || !comment.getMember().getId().equals(member.getId())) {
            throw new MemberException(INVALID_MEMBER);
        }
        comment.editNoteComment(request.content());
    }
}
