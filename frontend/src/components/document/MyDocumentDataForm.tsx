function MyDocumentDataForm() {
  // 신청인 정보 -데이터 연동되면 할당할 것

  return (
    <>
      <div className="mb-8">
        <h3 className="text-xl font-semibold mb-4">신청인 정보</h3>
        <div className="grid grid-cols-2 gap-x-8 gap-y-4">
          <div className="flex items-center">
            <span className="w-1/3 font-medium">성명</span>
            <span className="w-2/3 px-3 py-2">김씨피</span>
          </div>
          <div className="flex items-center">
            <span className="w-1/3 font-medium">국적</span>
            <span className="w-2/3 px-3 py-2">내국인</span>
          </div>
          <div className="flex items-center">
            <span className="w-1/3 font-medium">전화번호</span>
            <span className="w-2/3 px-3 py-2">010-3333-3333</span>
          </div>
          <div className="flex items-center">
            <span className="w-1/3 font-medium">생년월일</span>
            <span className="w-2/3 px-3 py-2">2024-01-02</span>
          </div>
          <div className="flex items-center">
            <span className="w-1/3 font-medium">이메일</span>
            <span className="w-2/3 px-3 py-2">ssafy@ssafy.com</span>
          </div>
          <div className="flex items-center">
            <span className="w-1/3 font-medium">주소</span>
            <span className="w-2/3 px-3 py-2">
              광주광역시 하남산단6번로 107
            </span>
          </div>
        </div>
      </div>
    </>
  );
}

export default MyDocumentDataForm;