import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useLogin } from "../contexts/AuthContext";
import {
  AppBar,
  Toolbar,
  Button,
  Typography,
  Menu,
  MenuItem,
  IconButton,
  Box,
  Tooltip,
  Drawer,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  Avatar,
  useTheme,
  useMediaQuery,
} from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import HomeIcon from "@mui/icons-material/Home";
import PersonIcon from "@mui/icons-material/Person";
import MessageIcon from "@mui/icons-material/Message";
import GroupIcon from "@mui/icons-material/Group";
import SettingsIcon from "@mui/icons-material/Settings";
import LogoutIcon from "@mui/icons-material/Logout";
import LoginIcon from "@mui/icons-material/Login";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import InventoryIcon from "@mui/icons-material/Inventory";
import ArticleIcon from "@mui/icons-material/Article";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";

export default function NavBar() {
  const { isLoggedIn, logout, loginUser } = useLogin();
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));

  // 상태관리
  const [anchorEl, setAnchorEl] = useState(null); // 드롭다운 메뉴 상태
  const [drawerOpen, setDrawerOpen] = useState(false); // 모바일 메뉴 상태

  // 이벤트 핸들러
  const handleLogout = () => {
    handleMenuClose();
    navigate("/logout");
  };

  const handleMenuOpen = (e) => setAnchorEl(e.currentTarget);
  const handleMenuClose = () => setAnchorEl(null);

  const handleDrawerOpen = () => setDrawerOpen(true);
  const handleDrawerClose = () => setDrawerOpen(false);

  const handleNavigate = (path) => {
    navigate(path);
    handleDrawerClose();
    handleMenuClose();
  };

  // 모바일 메뉴 렌더링
  const renderMobileMenu = () => (
    <Drawer
      anchor="left"
      open={drawerOpen}
      onClose={handleDrawerClose}
      PaperProps={{
        sx: { width: 280 },
      }}
    >
      <Box sx={{ py: 2, px: 2, display: "flex", alignItems: "center" }}>
        {isLoggedIn ? (
          <>
            <Avatar sx={{ bgcolor: "primary.main", mr: 2 }}>
              {loginUser?.charAt(0)?.toUpperCase() || "U"}
            </Avatar>
            <Typography variant="subtitle1" fontWeight="bold">
              {loginUser || "사용자"}
            </Typography>
          </>
        ) : (
          <Typography variant="h6" fontWeight="bold" sx={{ my: 1 }}>
            메뉴
          </Typography>
        )}
      </Box>

      <Divider />

      <List>
        <ListItem button onClick={() => handleNavigate("/")}>
          <ListItemIcon>
            <HomeIcon />
          </ListItemIcon>
          <ListItemText primary="홈" />
        </ListItem>

        {!isLoggedIn && (
          <>
            <ListItem button onClick={() => handleNavigate("/login")}>
              <ListItemIcon>
                <LoginIcon />
              </ListItemIcon>
              <ListItemText primary="로그인" />
            </ListItem>
            <ListItem button onClick={() => handleNavigate("/join")}>
              <ListItemIcon>
                <PersonAddIcon />
              </ListItemIcon>
              <ListItemText primary="회원가입" />
            </ListItem>
          </>
        )}

        {isLoggedIn && (
          <>
            <ListItem button onClick={() => handleNavigate("/profile")}>
              <ListItemIcon>
                <AccountCircleIcon />
              </ListItemIcon>
              <ListItemText primary="프로필" />
            </ListItem>

            <ListItem button onClick={() => handleNavigate("/member/list")}>
              <ListItemIcon>
                <GroupIcon />
              </ListItemIcon>
              <ListItemText primary="회원 목록" />
            </ListItem>

            <ListItem button onClick={() => handleNavigate("/items")}>
              <ListItemIcon>
                <InventoryIcon />
              </ListItemIcon>
              <ListItemText primary="상품 목록" />
            </ListItem>

            <ListItem button onClick={() => handleNavigate("/items/new")}>
              <ListItemIcon>
                <InventoryIcon />
              </ListItemIcon>
              <ListItemText primary="상품 등록" />
            </ListItem>

            <ListItem button onClick={() => handleNavigate("/order")}>
              <ListItemIcon>
                <ShoppingCartIcon />
              </ListItemIcon>
              <ListItemText primary="주문" />
            </ListItem>

            {/* 게시판 메뉴 추가 */}
            <ListItem button onClick={() => handleNavigate("/boards")}>
              <ListItemIcon>
                <ArticleIcon />
              </ListItemIcon>
              <ListItemText primary="게시판" />
            </ListItem>

            <Divider sx={{ my: 1 }} />

            <ListItem button onClick={() => handleNavigate("/simple/chat")}>
              <ListItemIcon>
                <MessageIcon />
              </ListItemIcon>
              <ListItemText primary="간단한 채팅" />
            </ListItem>

            <ListItem button onClick={() => handleNavigate("/chat")}>
              <ListItemIcon>
                <MessageIcon />
              </ListItemIcon>
              <ListItemText primary="채팅" />
            </ListItem>

            <ListItem
              button
              onClick={() => handleNavigate("/groupchatting/list")}
            >
              <ListItemIcon>
                <MessageIcon />
              </ListItemIcon>
              <ListItemText primary="그룹 채팅" />
            </ListItem>

            <ListItem button onClick={() => handleNavigate("/mychatpage")}>
              <ListItemIcon>
                <MessageIcon />
              </ListItemIcon>
              <ListItemText primary="마이페이지" />
            </ListItem>

            <Divider sx={{ my: 1 }} />

            <ListItem button onClick={() => handleNavigate("/settings")}>
              <ListItemIcon>
                <SettingsIcon />
              </ListItemIcon>
              <ListItemText primary="설정" />
            </ListItem>

            <ListItem button onClick={handleLogout}>
              <ListItemIcon>
                <LogoutIcon />
              </ListItemIcon>
              <ListItemText primary="로그아웃" />
            </ListItem>
          </>
        )}
      </List>
    </Drawer>
  );

  return (
    <>
      <AppBar
        position="static"
        sx={{
          bgcolor: "primary.main",
          boxShadow: 2,
        }}
      >
        <Toolbar>
          {isMobile && (
            <IconButton
              edge="start"
              color="inherit"
              onClick={handleDrawerOpen}
              sx={{ mr: 2 }}
            >
              <MenuIcon />
            </IconButton>
          )}

          <Typography
            variant="h6"
            component={Link}
            to="/"
            sx={{
              flexGrow: 1,
              textDecoration: "none",
              color: "#fff",
              fontWeight: "bold",
            }}
          >
            채팅 애플리케이션
          </Typography>

          {/* 데스크톱 메뉴 */}
          {!isMobile && (
            <Box sx={{ display: "flex" }}>
              <Button color="inherit" component={Link} to="/" sx={{ mx: 1 }}>
                홈
              </Button>

              {/* 로그인하지 않은 경우 */}
              {!isLoggedIn && (
                <>
                  <Button
                    color="inherit"
                    component={Link}
                    to="/login"
                    sx={{ mx: 1 }}
                    startIcon={<LoginIcon />}
                  >
                    로그인
                  </Button>
                  <Button
                    color="inherit"
                    component={Link}
                    to="/join"
                    sx={{ mx: 1 }}
                    startIcon={<PersonAddIcon />}
                  >
                    회원가입
                  </Button>
                </>
              )}

              {/* 로그인한 경우 */}
              {isLoggedIn && (
                <>
                  <Button
                    color="inherit"
                    component={Link}
                    to="/chat"
                    sx={{ mx: 1 }}
                    startIcon={<MessageIcon />}
                  >
                    채팅
                  </Button>
                  <Button
                    color="inherit"
                    component={Link}
                    to="/groupchatting/list"
                    sx={{ mx: 1 }}
                    startIcon={<GroupIcon />}
                  >
                    그룹 채팅
                  </Button>
                  <Button
                    color="inherit"
                    component={Link}
                    to="/items"
                    sx={{ mx: 1 }}
                    startIcon={<InventoryIcon />}
                  >
                    상품
                  </Button>
                  <Button
                    color="inherit"
                    component={Link}
                    to="/boards"
                    sx={{ mx: 1 }}
                    startIcon={<ArticleIcon />}
                  >
                    게시판
                  </Button>
                  <Button
                    color="inherit"
                    component={Link}
                    to="/mychatpage"
                    sx={{ mx: 1 }}
                    startIcon={<PersonIcon />}
                  >
                    마이페이지
                  </Button>
                  <Button
                    color="inherit"
                    component={Link}
                    to="/items/new"
                    sx={{ mx: 1 }}
                    startIcon={<InventoryIcon />}
                  >
                    상품 등록
                  </Button>
                  <Button
                    color="inherit"
                    component={Link}
                    to="/order"
                    sx={{ mx: 1 }}
                    startIcon={<ShoppingCartIcon />}
                  >
                    주문
                  </Button>

                  <Tooltip title="설정">
                    <IconButton color="inherit" onClick={handleMenuOpen}>
                      <SettingsIcon />
                    </IconButton>
                  </Tooltip>

                  <Menu
                    anchorEl={anchorEl}
                    open={Boolean(anchorEl)}
                    onClose={handleMenuClose}
                  >
                    <MenuItem onClick={() => handleNavigate("/profile")}>
                      <ListItemIcon>
                        <AccountCircleIcon fontSize="small" />
                      </ListItemIcon>
                      프로필
                    </MenuItem>
                    <MenuItem
                      onClick={() => handleNavigate("/settings/preferences")}
                    >
                      <ListItemIcon>
                        <SettingsIcon fontSize="small" />
                      </ListItemIcon>
                      환경설정
                    </MenuItem>
                    <MenuItem onClick={() => handleNavigate("/settings")}>
                      <ListItemIcon>
                        <SettingsIcon fontSize="small" />
                      </ListItemIcon>
                      계정 설정
                    </MenuItem>
                    <Divider />
                    <MenuItem onClick={handleLogout}>
                      <ListItemIcon>
                        <LogoutIcon fontSize="small" />
                      </ListItemIcon>
                      로그아웃
                    </MenuItem>
                  </Menu>
                </>
              )}
            </Box>
          )}
        </Toolbar>
      </AppBar>

      {/* 모바일 메뉴 */}
      {renderMobileMenu()}
    </>
  );
}
