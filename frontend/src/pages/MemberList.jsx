import React, { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Card, CardHeader, CardContent, Button } from "@mui/material";
import fetchMemberList from "../services/fetchMemberList";
import createChatRoom from "../services/createChatRoom";

const MemberList = () => {
  const [memberList, setMemberList] = useState([]); // 초기 상태를 빈 배열([])로 설정
  const [loading, setLoading] = useState(true); // 로딩 상태 추가
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const loadMembers = async () => {
      try {
        setLoading(true);
        // 새로 만든 fetchMemberList 서비스 사용
        const data = await fetchMemberList(navigate, location);

        if (data && Array.isArray(data)) {
          setMemberList(data);
          console.log("회원 목록 로드 성공:", data);
        } else {
          console.error("회원 목록 데이터가 잘못되었습니다.");
          setMemberList([]);
        }
      } catch (error) {
        console.error("회원 목록을 불러오는 중 오류 발생:", error);
        setMemberList([]);
      } finally {
        setLoading(false);
      }
    };

    loadMembers();
  }, [navigate, location]);

  const startChat = async (otherMemberId) => {
    try {
      // 새로 만든 createChatRoom 서비스 사용
      const roomId = await createChatRoom(otherMemberId, navigate, location);

      if (roomId) {
        navigate(`/chatpage/${roomId}`);
      } else {
        alert("채팅방 생성에 실패했습니다.");
      }
    } catch (error) {
      console.error("채팅방 생성 중 오류 발생:", error);
    }
  };

  return (
    <div className="container mx-auto p-4">
      <Card>
        <CardHeader
          title="회원 목록"
          sx={{ textAlign: "center", fontSize: "1.25rem", fontWeight: "bold" }}
        />
        <CardContent>
          {loading ? (
            <p className="text-center text-gray-500">
              회원 목록을 불러오는 중...
            </p>
          ) : memberList.length === 0 ? (
            <p className="text-center text-gray-500">회원 목록이 없습니다.</p>
          ) : (
            <table className="w-full border-collapse border border-gray-300">
              <thead>
                <tr className="bg-gray-100">
                  <th className="border p-2">ID</th>
                  <th className="border p-2">이름</th>
                  <th className="border p-2">Email</th>
                  <th className="border p-2">채팅</th>
                </tr>
              </thead>
              <tbody>
                {memberList.map((member) => (
                  <tr key={member.id} className="text-center">
                    <td className="border p-2">{member.id}</td>
                    <td className="border p-2">{member.name}</td>
                    <td className="border p-2">{member.email}</td>
                    <td className="border p-2">
                      <Button
                        variant="contained"
                        color="primary"
                        sx={{ padding: "6px 12px", borderRadius: "4px" }}
                        onClick={() => startChat(member.id)}
                      >
                        채팅하기
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default MemberList;
