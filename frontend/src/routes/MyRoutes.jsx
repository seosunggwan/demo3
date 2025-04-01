import { Routes, Route } from "react-router-dom";
import JoinForm from "../pages/Join";
import LoginForm from "../pages/Login";
import Home from "../pages/Home";
import OAuth2Redirect from "../services/Oauth2Redirect";
import Admin from "../pages/Admin";
import Logout from "../pages/Logout";
import MemberList from "../pages/MemberList";
import SimpleWebsocket from "../pages/SimpleWebsocket";
import StompChatPage from "../pages/StompChatPage";
import GroupChattingList from "../pages/GroupChattingList";
import MyChatPage from "../pages/MyChatPage";
import ChatPage from "../pages/ChatPage";
import NotFound from "../pages/NotFound";
import ItemList from "../pages/ItemList";
import ItemForm from "../pages/ItemForm";
import OrderList from "../pages/OrderList";
import OrderForm from "../pages/OrderForm";
import BoardList from "../pages/BoardList";
import BoardDetail from "../pages/BoardDetail";
import BoardForm from "../pages/BoardForm";
import Profile from "../pages/Profile";
import { useLogin } from "../contexts/AuthContext";
import PopularBoards from "../components/PopularBoards";

export default function MyRoutes() {
  const { isLoggedIn } = useLogin();

  return (
    <>
      <Routes>
        <Route path="/" element={<Home />} />
        {!isLoggedIn && (
          <>
            <Route path="/login" element={<LoginForm />} />
            <Route path="/join" element={<JoinForm />} />
          </>
        )}
        {isLoggedIn && (
          <>
            <Route path="/logout" element={<Logout />} />
            <Route path="/profile" element={<Profile />} />
          </>
        )}
        <Route path="/admin" element={<Admin />} />
        <Route path="/home" element={<Home />} />
        <Route path="/oauth2-jwt-header" element={<OAuth2Redirect />} />
        <Route path="/member/list" element={<MemberList />} />
        <Route path="/simple/chat" element={<SimpleWebsocket />} />
        <Route path="/chatpage/:roomId" element={<StompChatPage />} />
        <Route path="/groupchatting/list" element={<GroupChattingList />} />
        <Route path="/mychatpage" element={<MyChatPage />} />
        <Route path="/chat" element={<ChatPage />} />

        {/* 상품 관련 라우트 */}
        <Route path="/items" element={<ItemList />} />
        <Route path="/items/new" element={<ItemForm />} />
        <Route path="/items/:id/edit" element={<ItemForm />} />

        {/* 주문 관련 라우트 */}
        <Route path="/orders" element={<OrderList />} />
        <Route path="/order" element={<OrderForm />} />

        {/* 게시글 관련 라우트 */}
        <Route path="/boards" element={<BoardList />} />
        <Route path="/boards/popular" element={<PopularBoards />} />
        <Route path="/boards/:id" element={<BoardDetail />} />
        <Route path="/boards/new" element={<BoardForm />} />
        <Route path="/boards/:id/edit" element={<BoardForm />} />

        {/* 404 페이지는 항상 마지막에 위치 */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </>
  );
}
