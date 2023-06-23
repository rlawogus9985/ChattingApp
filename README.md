# ChattingApp
ChattingApp인턴과제본. socket과 api통신을 통해 구현됨

socket과 api콜한 내용은 아래와 같다.

================== SOCKET ==================
Lobby : RqAuthUser, {"memNo" : 10002}, 회원인증
Lobby : Rq1On1TextChat, {"textChatInfo":{"msg":"hello world"}, "commonRq1On1ChatInfo":{"replyMsgNo" : 0, "fromMemNo" : 10001, "toMemNo" : 10002}}, 1:1채팅
Lobby : RqRead1On1Chat, {"readMsgNos" : [1686561831400, 1686561833390, 1686561834798], "fromMemNo" : 10003, "toMemNo" : 10001}, 1:1채팅 읽음. Nt로 보낸것중 MsgNo가 있는 채팅메세지중 본인이 안읽은것 전부 보냄.
Lobby : RqDelete1On1Chat, {"delMsgNo" : 0, "fromMemNo" : 10001, "toMemNo" : 10002}, 1:1채팅 삭제
Lobby : RqJoinParty, {"partyNo":7, "ownerMemNo" : 10001, "rqMemNo" : 10002}, 파티입장
Lobby : RqAcceptParty, {"isAccept" : true, "rqJoinParty":{"partyNo":7, "ownerMemNo" : 10001, "rqMemNo" : 10002}} 파티장 파티참여 수락
Party : RqPartyTextChat, {"textChatInfo":{"msg":"hello world"}, "commonRqPartyChatInfo":{"replyMsgNo" : 0, "fromMemNo" : 10001, "partyNo" : 2}}, 파티채팅
Party : RqReadPartyChat, {"readMsgNos" : [1686561831400, 1686561833390, 1686561834798], "fromMemNo": 10002, "partyNo": 1}, 파티채팅 읽음. Nt로 보낸것중 MsgNo가 있는 채팅메세지중 본인이 안읽은것 전부 보냄.
Party : RqDeletePartyChat, {"delMsgNo" : 0, "partyNo": 1, "fromMemNo": 10001}, 파티채팅 삭제
Party : RqLeaveParty, {"partyNo": 1, "memNo": 10002}, 파티떠나기
Party : RqKickoutUser, {"partyNo": 1, "ownerMemNo": 10001, "kickoutMemNo":10004}, 유저강퇴

================== API ==================
POST : http://[address]/Rq1On1ChatLog, {"fromMemNo" : 10002, "toMemNo" : 10001, "lastMsgNo" : 1785593648377612600, "countPerPage" : 30, "sortType" : true}, 회원과의 1:1채팅로그 요청. 처음 불러올때 lastMsgNo는 현재 시간 millSec값, sortType : false(내림차순), true(오름차순)
POST : http://[address]/RqPartyChatLog, {"partyNo" : 2, "rqMemNo" : 10001, "lastMsgNo" : 1785593648377612600, "countPerPage" : 30, "sortType" : true}, 회원과의 파티채팅로그 요청. 처음 불러올때 lastMsgNo는 현재 시간 millSec값, sortType : false(내림차순), true(오름차순).
POST : http://[address]/RqSummaryPartyList, {"dongCode": "1", "timeStamp":21230517163022, "CountPerPage":30}, 방 정보 요청
POST : http://[address]/RqDetailPartyContext, {"partyNo": 1}, 방 상세정보
POST : http://[address]/RqPartyMemberList, {"partyNo": 1, "ownerMemNo": 10001, "timeStamp":20230517163022, "CountPerPage":30}, 방 멤버 요청
POST : http://[address]/RqSummaryUserInfo, {"memNo": 10001}, 회원 요약정보 요청
POST : http://[address]/RqDestroyPartyContext, {"partyNo": 1, "ownerMemNo" : 10001}, 방 삭제



POST : http://[address]/RqCreatePartyContext, {"partyNo": 1}, 방 생성

  {
     "summaryPartyInfo": {
       "memNo" : 10001,
       "mainPhotoUrl" : "https://demo.ycart.kr/shopboth_farm_max5_001/bbs/view_image.php?fn=http%3A%2F%2Fdemo.ycart.kr%2Fshopboth_cosmetics_001%2Fdata%2Feditor%2F1612%2Fcd2f39a0598c81712450b871c218164f_1482469221_493.jpg",
       "title" : "마이크테스트 아아아",
       "location" : "서구 마륵동",
       "maxMemberCount" : 2,
       "startTime" : 1682235334,
       "endTime" : 1682237334,
       "isAutoJoin" : false
    },

    "subPhotoUrlList" : ["https://www.google.com/url?sa=i&url=https%3A%2F%2Fkmong.com%2Fportfolio%2Fview%2F10972&psig=AOvVaw1QRGeIV66CEaQIx_Qgd3ih&ust=1682829365123000&source=images&cd=vfe&ved=0CBEQjRxqFwoTCOD5zYaizv4CFQAAAAAdAAAAABAE","https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.leagueoflegends.com%2Fko-kr%2Fchampions%2Fzed%2F&psig=AOvVaw3dPMEtuIrslBiQ1Tmkm92u&ust=1682829392161000&source=images&cd=vfe&ved=0CBEQjRxqFwoTCKDlmJOizv4CFQAAAAAdAAAAABAE","https://www.google.com/url?sa=i&url=https%3A%2F%2Fkr.freepik.com%2Fphotos%2F%25EC%25BA%2594%25EB%2594%2594&psig=AOvVaw28t0cJAk2wY9TrX7u1iviS&ust=1682829409643000&source=images&cd=vfe&ved=0CBEQjRxqFwoTCKj12Jyizv4CFQAAAAAdAAAAABAN"],
    "questContent" : "마이크 테스트 방입니다.~~~컴온~ 컴온.~~~"
  }
===========================================

현재 가능한 기능. 
1. 로그인
2. 1대1 채팅
3. 파티(그룹) 채팅
4. 채팅 삭제
5. 방 만들기
6. 비밀방 신청넣기
7. 신청 허락하기
8. 유저 강퇴
9. 방 탈퇴
10. 방 삭제
11. 등등 ...

현재 있는 버그
1. 파티 채팅 읽음 처리를 하는 과정에서 파티 내역목록이 2번가져와지는 현상
2. Room DB를 통해 접속기록을 확인하기 때문에 여러 다른 기기에서 접속했을때 중복 읽음 처리가 되는 현상
3. 모든 사람이 파티 방에 들어와있을때 내가 채팅을 치면 채팅을 읽었다는 소켓통신이 한꺼번에 들어와서 비동기 과정에서 숫자가 비정상적으로 줄어드는 현상

추가 하고 싶은 기능
1. 현재 방에 내가 읽지 않은 채팅이 몇개가 있는지 표시(1대1이든, 파티방이든)
2. 디자인 예쁘게 꾸미기
   
