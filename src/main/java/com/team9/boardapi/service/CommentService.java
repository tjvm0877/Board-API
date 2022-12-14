package com.team9.boardapi.service;


import com.team9.boardapi.dto.CommentRequestDto;
import com.team9.boardapi.dto.CommentResponseDto;
import com.team9.boardapi.entity.Comment;
import com.team9.boardapi.entity.Post;

import com.team9.boardapi.entity.Comment;
import com.team9.boardapi.entity.CommentLike;
import com.team9.boardapi.entity.User;
import com.team9.boardapi.entity.UserRoleEnum;
import com.team9.boardapi.mapper.CommentMapper;
import com.team9.boardapi.repository.CommentLikeRepository;
import com.team9.boardapi.repository.CommentRepository;
import com.team9.boardapi.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;
    private final CommentLikeRepository commentLikeRepository;

    /*---- 댓글 생성 ----*/
    @Transactional
    public ResponseEntity<CommentResponseDto> createComment(Long postId, CommentRequestDto commentRequestDto, User user) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("게시글이 존재하지 않습니다")
        );

        Comment comment = commentMapper.toEntity(commentRequestDto, user, post);
        commentRepository.save(comment);

        CommentResponseDto response = commentMapper.commentToCommentResponseDto(comment, user);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    /*---- 댓글 수정 ----*/
    @Transactional
    public ResponseEntity<CommentResponseDto> updateComment(Long id, CommentRequestDto commentRequestDto, User user) {
        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("선택한 댓글을 찾을 수 없습니다")
        );

        //사용자 권한에 따른 댓글 수정
        UserRoleEnum userRoleEnum = user.getRole();

        //댓글을 작성한 사용자와 동일한 사용자가 아닐 때, 권한이 관리자가 아닐 때
        if(userRoleEnum != UserRoleEnum.ADMIN && !comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("수정을 할 수 없습니다.");
        }

        comment.update(commentRequestDto.getContent());

        CommentResponseDto commentResponseDto = new CommentResponseDto(comment, user);
        return new ResponseEntity<>(commentResponseDto,HttpStatus.OK);
    }

    /*---- 댓글 삭제 ----*/
    @Transactional
    public ResponseEntity<String> deleteComment(Long commentId, User user) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다.")
        );
        UserRoleEnum userRoleEnum = user.getRole();

        // 댓글을 작성한 유저가 아닐 때, 혹은 관리자가 아닐 때 Exception
        if(userRoleEnum != UserRoleEnum.ADMIN && !comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        // 입력된 댓글 id의 좋아요 모두 삭제
        commentLikeRepository.deleteAllByComment_Id(commentId);

        // 입력된 commentId를 가진 댓글 삭제
        commentRepository.deleteById(commentId);

        // 결과 반환
        return new ResponseEntity<String>("댓글 삭제 성공", HttpStatus.OK);
    }

    /*---- 댓글 좋아요 등록/취소 ----*/
    @Transactional
    public ResponseEntity<String> like(long commentId, User user) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다.")
        );
        Optional<CommentLike> like = commentLikeRepository.findByUser_Id(user.getId());

        if (like.isPresent()) { // 이미 좋아요가 존재할 때
            CommentLike selectedLike = like.get();
            // 좋아요를 삭제
            commentLikeRepository.delete(selectedLike);
            comment.unlike();
            return new ResponseEntity<String>("좋아요를 취소하였습니다.", HttpStatus.OK);
        } else { // 좋아요가 존재하지 않을 때
            CommentLike newLike = new CommentLike(user, comment);
            // 좋아요를 등록(저장)
            comment.like();
            commentLikeRepository.save(newLike);
            return new ResponseEntity<String>("좋아요를 등록했습니다.", HttpStatus.OK);
        }
    }
}