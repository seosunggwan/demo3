import React, { useState, useEffect } from "react";
import {
  Box,
  Typography,
  Divider,
  CircularProgress,
  TextField,
  Button,
  List,
  ListItem,
  Avatar,
  IconButton,
  Menu,
  MenuItem,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@mui/material";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import ReplyIcon from "@mui/icons-material/Reply";
import { useLogin } from "../contexts/AuthContext";
import {
  getCommentsByBoardId,
  createComment,
  updateComment,
  deleteComment,
} from "../services/commentService";
import { formatDistanceToNow, isValid } from "date-fns";
import { ko } from "date-fns/locale";

/**
 * 댓글 목록 컴포넌트
 *
 * @param {Object} props - 컴포넌트 속성
 * @param {number} props.boardId - 게시글 ID
 */
const CommentList = ({ boardId }) => {
  const { isLoggedIn } = useLogin();
  const [comments, setComments] = useState([]);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [newComment, setNewComment] = useState("");
  const [replyingTo, setReplyingTo] = useState(null);
  const [replyContent, setReplyContent] = useState("");
  const [editingComment, setEditingComment] = useState(null);
  const [editContent, setEditContent] = useState("");
  const [anchorEl, setAnchorEl] = useState(null);
  const [selectedComment, setSelectedComment] = useState(null);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);

  // 게시글 ID가 변경될 때마다 댓글 목록 불러오기
  useEffect(() => {
    if (boardId) {
      fetchComments();
    }
  }, [boardId]);

  // 댓글 목록 불러오기
  const fetchComments = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getCommentsByBoardId(boardId);
      setComments(response.comments || []);
      setTotalCount(response.totalCount || 0);
    } catch (err) {
      setError(err.message || "댓글을 불러오는데 실패했습니다.");
      console.error("댓글 목록 불러오기 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  // 댓글 작성 처리
  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!isLoggedIn) {
      alert("로그인 후 댓글을 작성할 수 있습니다.");
      return;
    }

    if (!newComment.trim()) return;

    try {
      setLoading(true);
      await createComment(boardId, { content: newComment });
      setNewComment("");
      fetchComments(); // 댓글 목록 새로고침
    } catch (err) {
      setError(err.message || "댓글 작성에 실패했습니다.");
      console.error("댓글 작성 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  // 대댓글 작성 처리
  const handleReplySubmit = async (e) => {
    e.preventDefault();
    if (!isLoggedIn || !replyingTo) return;
    if (!replyContent.trim()) return;

    try {
      setLoading(true);
      await createComment(boardId, {
        content: replyContent,
        parentId: replyingTo.id,
      });
      setReplyingTo(null);
      setReplyContent("");
      fetchComments(); // 댓글 목록 새로고침
    } catch (err) {
      setError(err.message || "대댓글 작성에 실패했습니다.");
      console.error("대댓글 작성 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  // 댓글 수정 처리
  const handleUpdateComment = async () => {
    if (!editingComment || !editContent.trim()) return;

    try {
      setLoading(true);
      await updateComment(boardId, editingComment.id, { content: editContent });
      setEditingComment(null);
      setEditContent("");
      fetchComments(); // 댓글 목록 새로고침
    } catch (err) {
      setError(err.message || "댓글 수정에 실패했습니다.");
      console.error("댓글 수정 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  // 댓글 삭제 처리
  const handleDeleteComment = async () => {
    if (!selectedComment) return;

    try {
      setLoading(true);
      await deleteComment(boardId, selectedComment.id);
      setDeleteConfirmOpen(false);
      setSelectedComment(null);
      fetchComments(); // 댓글 목록 새로고침
    } catch (err) {
      setError(err.message || "댓글 삭제에 실패했습니다.");
      console.error("댓글 삭제 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  // 메뉴 열기
  const handleMenuOpen = (event, comment) => {
    setAnchorEl(event.currentTarget);
    setSelectedComment(comment);
  };

  // 메뉴 닫기
  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedComment(null);
  };

  // 삭제 확인 다이얼로그 열기
  const handleDeleteDialogOpen = () => {
    setDeleteConfirmOpen(true);
    setAnchorEl(null);
  };

  // 수정 모드 활성화
  const handleEditMode = () => {
    setEditingComment(selectedComment);
    setEditContent(selectedComment.content);
    setAnchorEl(null);
  };

  // 날짜 포맷 유틸리티 함수
  const formatCommentDate = (dateString) => {
    if (!dateString) return "날짜 정보 없음";

    const date = new Date(dateString);
    if (!isValid(date)) return "날짜 형식 오류";

    try {
      return formatDistanceToNow(date, {
        addSuffix: true,
        locale: ko,
      });
    } catch (error) {
      console.error("날짜 포맷 오류:", error);
      return "날짜 변환 오류";
    }
  };

  // 댓글 컴포넌트
  const Comment = ({ comment, depth = 0 }) => {
    const isAuthor =
      isLoggedIn &&
      comment.authorId === JSON.parse(localStorage.getItem("user"))?.id;
    const isEditing = editingComment && editingComment.id === comment.id;
    const isDeleted = comment.deleted;

    return (
      <>
        <ListItem
          sx={{
            flexDirection: "column",
            alignItems: "flex-start",
            pl: depth > 0 ? 6 : 2,
            pr: 2,
            py: 1,
            bgcolor: depth > 0 ? "rgba(0, 0, 0, 0.02)" : "transparent",
            borderLeft: depth > 0 ? "3px solid #e0e0e0" : "none",
          }}
        >
          {/* 댓글 헤더 (작성자 정보 및 메뉴) */}
          <Box
            sx={{
              display: "flex",
              width: "100%",
              justifyContent: "space-between",
              alignItems: "center",
              mb: 1,
            }}
          >
            <Box sx={{ display: "flex", alignItems: "center" }}>
              <Avatar
                sx={{ width: 32, height: 32, mr: 1 }}
                alt={comment.authorName}
                src="/static/images/avatar/1.jpg"
              />
              <Box>
                <Typography variant="subtitle2" component="span">
                  {comment.authorName}
                </Typography>
                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ ml: 1 }}
                >
                  {formatCommentDate(comment.createdTime)}
                </Typography>
              </Box>
            </Box>

            {isAuthor && !isDeleted && (
              <IconButton
                size="small"
                onClick={(e) => handleMenuOpen(e, comment)}
              >
                <MoreVertIcon fontSize="small" />
              </IconButton>
            )}
          </Box>

          {/* 댓글 내용 */}
          {isEditing ? (
            <Box sx={{ width: "100%", mb: 1 }}>
              <TextField
                fullWidth
                multiline
                minRows={2}
                maxRows={5}
                value={editContent}
                onChange={(e) => setEditContent(e.target.value)}
                placeholder="댓글 내용을 입력하세요."
                variant="outlined"
                size="small"
              />
              <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 1 }}>
                <Button
                  variant="outlined"
                  size="small"
                  onClick={() => setEditingComment(null)}
                  sx={{ mr: 1 }}
                >
                  취소
                </Button>
                <Button
                  variant="contained"
                  size="small"
                  onClick={handleUpdateComment}
                  disabled={loading}
                >
                  저장
                </Button>
              </Box>
            </Box>
          ) : (
            <Typography
              variant="body2"
              sx={{
                width: "100%",
                whiteSpace: "pre-wrap",
                color: isDeleted ? "text.disabled" : "text.primary",
              }}
            >
              {comment.content}
            </Typography>
          )}

          {/* 댓글 푸터 (답글 버튼) */}
          {!isDeleted && depth < 1 && isLoggedIn && (
            <Box
              sx={{
                mt: 1,
                display: "flex",
                justifyContent: "flex-end",
                width: "100%",
              }}
            >
              <Button
                size="small"
                startIcon={<ReplyIcon />}
                onClick={() => setReplyingTo(comment)}
                sx={{ textTransform: "none" }}
              >
                답글
              </Button>
            </Box>
          )}

          {/* 대댓글 작성 폼 */}
          {replyingTo && replyingTo.id === comment.id && (
            <Box
              component="form"
              onSubmit={handleReplySubmit}
              sx={{
                width: "100%",
                display: "flex",
                flexDirection: "column",
                alignItems: "flex-end",
                mt: 2,
                mb: 1,
                pl: 4,
              }}
            >
              <TextField
                fullWidth
                multiline
                minRows={2}
                maxRows={4}
                placeholder="답글을 입력하세요."
                value={replyContent}
                onChange={(e) => setReplyContent(e.target.value)}
                variant="outlined"
                size="small"
              />
              <Box sx={{ mt: 1 }}>
                <Button
                  variant="outlined"
                  size="small"
                  onClick={() => setReplyingTo(null)}
                  sx={{ mr: 1 }}
                >
                  취소
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  size="small"
                  disabled={!replyContent.trim() || loading}
                >
                  답글 작성
                </Button>
              </Box>
            </Box>
          )}
        </ListItem>

        {/* 대댓글 렌더링 */}
        {comment.children?.map((childComment) => (
          <Comment
            key={childComment.id}
            comment={childComment}
            depth={depth + 1}
          />
        ))}
      </>
    );
  };

  return (
    <Box sx={{ mt: 4 }}>
      <Typography variant="h6" gutterBottom>
        댓글 {totalCount}개
      </Typography>
      <Divider sx={{ mb: 2 }} />

      {/* 댓글 작성 폼 */}
      {isLoggedIn && (
        <Box component="form" onSubmit={handleCommentSubmit} sx={{ mb: 3 }}>
          <TextField
            fullWidth
            multiline
            minRows={3}
            placeholder="댓글을 작성하세요."
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            variant="outlined"
          />
          <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 1 }}>
            <Button
              type="submit"
              variant="contained"
              disabled={!newComment.trim() || loading}
            >
              {loading ? <CircularProgress size={24} /> : "댓글 작성"}
            </Button>
          </Box>
        </Box>
      )}

      {/* 에러 메시지 */}
      {error && (
        <Typography color="error" variant="body2" sx={{ mb: 2 }}>
          {error}
        </Typography>
      )}

      {/* 댓글 목록 */}
      {loading && comments.length === 0 ? (
        <Box sx={{ display: "flex", justifyContent: "center", my: 3 }}>
          <CircularProgress />
        </Box>
      ) : comments.length > 0 ? (
        <List disablePadding>
          {comments.map((comment) => (
            <Comment key={comment.id} comment={comment} />
          ))}
        </List>
      ) : (
        <Typography
          variant="body2"
          color="text.secondary"
          sx={{ textAlign: "center", my: 3 }}
        >
          첫 댓글을 작성해보세요.
        </Typography>
      )}

      {/* 댓글 메뉴 */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={handleEditMode}>수정</MenuItem>
        <MenuItem onClick={handleDeleteDialogOpen}>삭제</MenuItem>
      </Menu>

      {/* 삭제 확인 다이얼로그 */}
      <Dialog
        open={deleteConfirmOpen}
        onClose={() => setDeleteConfirmOpen(false)}
      >
        <DialogTitle>댓글 삭제</DialogTitle>
        <DialogContent>
          <DialogContentText>
            이 댓글을 삭제하시겠습니까? 삭제된 댓글은 복구할 수 없습니다.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteConfirmOpen(false)}>취소</Button>
          <Button onClick={handleDeleteComment} color="error" autoFocus>
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default CommentList;
