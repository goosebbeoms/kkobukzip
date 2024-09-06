import { useRef, useState } from "react";
import { Helmet } from "react-helmet-async";
import { usePostcodeSearch } from "../../hooks/usePostcodeSearch";

interface AssigneeDocumentDataType {
  name: string;
  phoneNumber: string;
  address: string;
}

interface DocData {
  turtleUUID: string;
  count: number;
  purpose: string;
  transferReason: string;
  assignee?: AssigneeDocumentDataType;
}

interface FetchData {
  docType: "양수신청서";
  applicant: string;
  detail: DocData;
}

// 양수 서류 컴포넌트
function AssigneeDocument() {
  const { postcodeData, loadPostcodeSearch } = usePostcodeSearch();
  const addressBtnRef = useRef<HTMLButtonElement | null>(null);

  // 양수인 정보
  const [assignee, setAssignee] = useState<AssigneeDocumentDataType>({
    name: "",
    phoneNumber: "",
    address: "",
  });

  const [data, setData] = useState<DocData>({
    turtleUUID: "",
    count: 1,
    purpose: "연구",
    transferReason: "",
  });

  const [detailLocation, setDetailLocation] = useState<string>("");

  // 유저데이터 로드하는 함수
  const loadUserData = () => {
    console.log("loadUserData");
  };

  // 문서 작성 요청 함수
  const sendAssigneeDocRequest = () => {
    const docs: FetchData = {
      docType: "양수신청서",
      applicant: "sadfk3ld-3b7d-8012-9bdd-2b0182lscb6d",
      detail: {
        assignee: {
          ...assignee,
          address: assignee.address + " " + detailLocation,
        },
        ...data,
      },
    };

    console.log(docs);
  };

  // 양수자 수정 함수
  const changeAssigneeHandle = (
    key: keyof AssigneeDocumentDataType,
    evt: React.ChangeEvent<HTMLInputElement>
  ) => {
    setAssignee((prev) => ({ ...prev, [key]: evt.target.value }));
  };

  // 양수자 이외의 데이터 수정 함수
  const changeDataHandle = (
    key: keyof DocData,
    evt:
      | React.ChangeEvent<HTMLInputElement>
      | React.ChangeEvent<HTMLSelectElement>
  ) => {
    setData((prev) => ({ ...prev, [key]: evt.target.value }));
  };

  return (
    <>
      <Helmet>
        <title>양수서류작성</title>
      </Helmet>

      <div className="mb-8">
        <div className="w-full flex">
          <span className="text-xl font-semibold mb-4 flex-1">양수인</span>
          <label
            htmlFor="loadUserData"
            className="cursor-pointer select-none mr-4"
          >
            <span>신청인 정보 불러오기</span>
            <input onClick={loadUserData} type="checkbox" id="loadUserData" />
          </label>
        </div>

        {/* 양수인 정보 */}
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="flex items-center">
              <span className="w-1/3 font-medium">성명(상호)</span>
              <input
                type="text"
                onChange={(evt) => changeAssigneeHandle("name", evt)}
                placeholder="성명(상호)"
                className="w-2/3 px-3 py-2 border rounded"
              />
            </div>

            <div className="flex items-center">
              <span className="w-1/3 font-medium">전화번호</span>
              <input
                type="number"
                placeholder="전화번호"
                onChange={(evt) => changeAssigneeHandle("phoneNumber", evt)}
                className="w-2/3 px-3 py-2 border rounded"
              />
            </div>
          </div>
          <div className="flex items-center">
            <label className="w-1/4 font-medium">주소</label>

            <button
              ref={addressBtnRef}
              className="w-1/12"
              onClick={loadPostcodeSearch}
            >
              찾기
            </button>
            <input
              type="text"
              className="w-1/3 px-3 py-2 border rounded ml-2"
              placeholder="기본주소"
              readOnly
              onChange={(evt) => changeAssigneeHandle("address", evt)}
              value={postcodeData?.roadAddress || ""}
              onClick={() => addressBtnRef.current?.click()}
            />
            <input
              type="text"
              onChange={(evt) => {
                setDetailLocation(evt.target.value);
              }}
              className="w-1/3 px-3 py-2 border rounded ml-2"
              placeholder="상세주소"
            />
          </div>
        </div>
      </div>
      {/* 양수인 정보 끝 */}

      {/* 개체 정보 - 사전에 설정된 정보 불러올 것 */}
      <div className="mb-8">
        <h3 className="text-xl font-semibold mb-4">개체 정보 작성</h3>
        <div className="grid grid-cols-2 gap-4">
          <div className="flex items-center">
            <span className="w-1/3 font-medium">학명</span>
            <span className="w-2/3 px-3 py-2">Malaclemys terrapin</span>
          </div>
          <div className="flex items-center">
            <span className="w-1/3 font-medium">보통명(일반명)</span>
            <span className="w-2/3 px-3 py-2">(공란)</span>
          </div>
          <div className="flex items-center">
            <span className="w-1/3 font-medium">부속서등급</span>
            <span className="w-2/3 px-3 py-2">II급</span>
          </div>
          <div className="flex items-center">
            <label className="w-1/3 font-medium">수량</label>
            <span className="w-2/3 px-3 py-2">1</span>
          </div>
          <div className="flex items-center">
            <span className="w-1/3 font-medium">형태</span>
            <span className="w-2/3 px-3 py-2">살아있는 생물</span>
          </div>
          <div className="flex items-center">
            <label className="w-1/3 font-medium">용도</label>
            <select
              onChange={(evt) => changeDataHandle("purpose", evt)}
              className="font-medium bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-2/3 p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
            >
              <option value="연구">연구</option>
              <option value="학술">학술</option>
              <option value="애완">애완</option>
              <option value="상업">상업</option>
            </select>
          </div>
          <div className="flex items-center ">
            <span className="w-1/3 font-medium">양수사유</span>
            <input
              type="text"
              onChange={(evt) => changeDataHandle("transferReason", evt)}
              value={data.transferReason || ""}
              className="w-2/3 px-3 py-2 border rounded"
              placeholder="양수사유"
            />
          </div>
          <div className="flex items-center">
            <label className="w-1/3 font-medium">개체식별번호</label>
            <input
              type="text"
              onChange={(evt) => changeDataHandle("turtleUUID", evt)}
              className="w-2/3 px-3 py-2 border rounded"
              placeholder="개체식별번호"
            />
          </div>
        </div>
      </div>
      {/* 개체 정보 끝 */}

      <div className="flex justify-end space-x-4">
        <button
          onClick={sendAssigneeDocRequest}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          신청서 제출
        </button>
      </div>
    </>
  );
}

export default AssigneeDocument;