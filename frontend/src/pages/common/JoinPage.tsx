import { useCallback, useEffect, useState, useRef } from "react";
import { Helmet } from "react-helmet-async";
import { usePostcodeSearch } from "../../hooks/usePostcodeSearch";
import useDeviceStore from "../../store/useDeviceStore";
import ErrorMessage from "../../components/common/join/ErrorMessage";
import { JoinDataType } from "../../types/join";
import { register } from "../../apis/userApi";
import Header from "../../components/common/Header";
import StopTurtleImg from "../../assets/turtle_home_stop.png";

interface ErrorStateType {
  email: string;
  password: string;
  passwordConfirm: string;
  name: string;
  nickname: string;
  birthday: string;
  phoneNumber: string;
  address: string;
  detailedAddress: string;
}

function JoinPage() {
  const isMobile = useDeviceStore((state) => state.isMobile);
  const { postcodeData, loadPostcodeSearch } = usePostcodeSearch();
  const addressBtnRef = useRef<HTMLButtonElement | null>(null);
  const [step, setStep] = useState(1);
  const [data, setData] = useState<JoinDataType>({
    email: "",
    password: "",
    foreignFlag: false,
    name: "",
    nickname: "",
    birthday: "",
    phoneNumber: "",
    address: "",
  });
  const [detailedAddress, setDetailAddress] = useState("");

  const [birth, setBirth] = useState<{ y: number | null; m: number | null; d: number | null }>({ y: null, m: null, d: null });

  const [phoneNumber, setPhoneNumber] = useState<{ first: string | null; second: string | null; third: string | null }>({
    first: null,
    second: null,
    third: null,
  });

  const [confirmPassword, setConfirmPassword] = useState<string>("");

  const [errStat, setErrStat] = useState<ErrorStateType>({
    email: "",
    password: "",
    passwordConfirm: "",
    name: "",
    nickname: "",
    birthday: "",
    phoneNumber: "",
    address: "",
    detailedAddress: "",
  });

  useEffect(() => {
    if (postcodeData?.jibunAddress) {
      setData((prev) => ({
        ...prev,
        address: postcodeData.jibunAddress,
      }));
    }
  }, [postcodeData?.jibunAddress]);

  const onChangeHandle = (key: keyof JoinDataType) => (evt: React.ChangeEvent<HTMLInputElement>) => {
    setData((prev) => ({ ...prev, [key]: evt.target.value }));
  };

  const loadDaumPostcodeScript = useCallback(() => {
    const script = document.createElement("script");
    script.src = "//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
    script.async = true;
    document.head.appendChild(script);
  }, []);

  useEffect(() => {
    loadDaumPostcodeScript();
  }, [loadDaumPostcodeScript]);

  const handleChangeBirth = (evt: React.ChangeEvent<HTMLInputElement>, type: "y" | "m" | "d") => {
    const value = evt.target.value;
    setBirth((prev) => ({ ...prev, [type]: parseInt(value) }));
  };

  const handleChangeForeignFlag = (tf: boolean) => {
    setData((prev) => ({ ...prev, foreignFlag: tf }));
  };

  const changeAddress = (type: "address" | "detailedAddress") => (evt: React.ChangeEvent<HTMLInputElement>) => {
    const value = evt.target.value;
    setData((prev) => ({ ...prev, [type]: value }));
  };

  const confirmPasswordChangeHandle = (evt: React.ChangeEvent<HTMLInputElement>) => {
    setConfirmPassword(evt.target.value);

    if (data.password !== evt.target.value) {
      setErrStat((prev) => ({
        ...prev,
        passwordConfirm: "비밀번호가 일치하지 않습니다.",
      }));
    } else {
      setErrStat((prev) => ({ ...prev, passwordConfirm: "" }));
    }
  };

  const handleChangePhoneNumber = (type: "first" | "second" | "third") => (evt: React.ChangeEvent<HTMLInputElement>) => {
    const value = evt.target.value;
    setPhoneNumber((prev) => ({ ...prev, [type]: value }));
  };

  const validateEmail = (email: string) => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  };

  const validatePassword = (password: string) => {
    return password.length >= 8 && /[!@#$%^&*(),.?":{}|<>]/.test(password);
  };

  const validateBirthday = (y: number | null, m: number | null, d: number | null) => {
    if (!y || !m || !d) return false;
    const date = new Date(y, m - 1, d);

    return date.getFullYear() === y && date.getMonth() === m - 1 && date.getDate() === d;
  };

  const validatePhoneNumber = (first: string | null, second: string | null, third: string | null) => {
    return first && second && third && first.toString().length === 3 && second.toString().length === 4 && third.toString().length === 4;
  };

  const handleNextStep = () => {
    // Validate the first step fields before moving to the next step
    const newErrStat: ErrorStateType = {
      email: "",
      password: "",
      passwordConfirm: "",
      name: "",
      nickname: "",
      birthday: "",
      phoneNumber: "",
      address: "",
      detailedAddress: "",
    };

    let isValid = true;

    if (!validateEmail(data.email)) {
      newErrStat.email = "유효한 이메일 주소를 입력해주세요.";
      isValid = false;
    }

    if (!validatePassword(data.password)) {
      newErrStat.password = "비밀번호는 8자 이상이며, 특수문자를 포함해야 합니다.";
      isValid = false;
    }

    if (data.name.trim().length === 0) {
      newErrStat.name = "이름을 입력해주세요.";
      isValid = false;
    }

    if (data.nickname.trim().length === 0) {
      newErrStat.nickname = "닉네임을 입력해주세요.";
      isValid = false;
    }

    setErrStat(newErrStat);

    if (isValid) {
      setStep(2);
    }
  };

  const handleJoinSubmit = async () => {
    let isValid = true;
    const newErrStat: ErrorStateType = {
      email: "",
      password: "",
      passwordConfirm: "",
      name: "",
      nickname: "",
      birthday: "",
      phoneNumber: "",
      address: "",
      detailedAddress: "",
    };

    if (!validateBirthday(birth.y, birth.m, birth.d)) {
      newErrStat.birthday = "올바른 생년월일을 입력해주세요.";
      isValid = false;
    }

    if (!validatePhoneNumber(phoneNumber.first, phoneNumber.second, phoneNumber.third)) {
      newErrStat.phoneNumber = "올바른 전화번호를 입력해주세요.";
      isValid = false;
    }

    if (data.address.trim().length === 0) {
      newErrStat.address = "주소를 입력해주세요.";
      isValid = false;
    }

    if (detailedAddress.trim().length === 0) {
      newErrStat.detailedAddress = "상세 주소를 입력해주세요.";
      isValid = false;
    }

    setErrStat(newErrStat);

    if (isValid) {
      await register({ ...data, address: data.address + " " + detailedAddress });
    }
  };

  return (
    <>
      <Helmet>
        <title>회원가입</title>
      </Helmet>
      <Header />
      <div className="absolute top-16 md:top-0 left-0 right-0 md:px-[250px] flex justify-center items-center h-full">
        <div className="relative w-full bg-[#D5E5BD] backdrop-blur-sm rounded-[20px] shadow-[20px] z-10 grid md:grid-cols-2">
          {step === 1 && (
            <div className="p-2.5 my-10 order-2 md:order-1">
              <div className="w-full h-full rounded-l-[20px] m-auto flex justify-center items-center overflow-y-auto">
                <div className="w-4/5">
                  <h2 className="text-[38px] text-center mb-6 font-dnf-bitbit">회원가입</h2>
                  <form
                    onSubmit={(evt) => {
                      evt.preventDefault();
                      handleNextStep();
                    }}
                    className="grid grid-cols-1 space-y-4"
                  >
                    <div>
                      <div className="flex items-center">
                        <span className="w-1/3 font-medium">이메일</span>
                        <input type="email" id="email" value={data.email} onChange={onChangeHandle("email")} placeholder="이메일을 입력해주세요" aria-required="true" aria-label="이메일을 입력해주세요" className="text-[18px] w-2/3 px-3 py-2 border rounded bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]" />
                      </div>
                      {errStat.email && <ErrorMessage msg={errStat.email} />}
                    </div>

                    <div>
                      <div className="flex items-center">
                        <span className="w-1/3 font-medium">비밀번호</span>
                        <input type="password" id="password" value={data.password} onChange={onChangeHandle("password")} autoComplete="off" maxLength={20} size={20} aria-required="true" aria-invalid="false" placeholder="비밀번호를 입력해주세요" className="text-[18px] w-2/3 px-3 py-2 border rounded bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]" />
                      </div>
                      {errStat.password && <ErrorMessage msg={errStat.password} />}
                    </div>

                    <div>
                      <div className="flex items-center">
                        <span className="w-1/3 font-medium">비밀번호 확인</span>
                        <input type="password" id="password2" value={confirmPassword} onChange={confirmPasswordChangeHandle} autoComplete="off" maxLength={20} size={20} aria-required="true" aria-invalid="false" placeholder="비밀번호를 입력해주세요" className="text-[18px] w-2/3 px-3 py-2 border rounded bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]" />
                      </div>
                      {errStat.passwordConfirm && <ErrorMessage msg={errStat.passwordConfirm} />}
                    </div>

                    <div>
                      <div className="flex items-center">
                        <span className="w-1/3 font-medium">성명</span>
                        <input type="text" placeholder="성명을 입력해주세요." value={data.name} onChange={onChangeHandle("name")} aria-required="true" className="text-[18px] w-2/3 px-3 py-2 border rounded bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]" />
                      </div>
                      {errStat.name && <ErrorMessage msg={errStat.name} />}
                    </div>

                    <div>
                      <div className="flex items-center">
                        <span className="w-1/3 font-medium">닉네임</span>
                        <input type="text" value={data.nickname} onChange={onChangeHandle("nickname")} aria-required="true" placeholder="닉네임을 입력해주세요." className="text-[18px] w-2/3 px-3 py-2 border rounded bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]" />
                      </div>
                      {errStat.nickname && <ErrorMessage msg={errStat.nickname} />}
                    </div>

                    <button type="submit" className="w-full bg-[#4B721F] text-[22px] text-white py-3 rounded hover:bg-[#3E5A1E]">
                      다음
                    </button>
                  </form>
                </div>
              </div>
            </div>
          )}

          {step === 2 && (
            <div className="p-2.5 my-10 order-2 md:order-1">
              <div className="w-full h-full rounded-l-[20px] m-auto flex justify-center items-center overflow-y-auto">
                <div className="w-4/5">
                  <h2 className="text-3xl mt-2 md:mt-0 md:text-[38px] text-center mb-6 font-dnf-bitbit">회원가입 - 추가 정보</h2>
                  <form
                    onSubmit={(evt) => {
                      evt.preventDefault();
                      handleJoinSubmit();
                    }}
                    className="grid grid-cols-1 space-y-4"
                  >
                    {/* 생년월일 */}
                    <div>
                      <div className="flex flex-col md:flex-row items-center">
                        <span className="md:w-1/3 font-medium">생년월일</span>
                        <div className="md:w-2/3 flex justify-between">
                          <div className="text-[18px] w-4/12 flex items-center">
                            <input
                              type="number"
                              value={birth.y ?? ""}
                              onChange={(evt) => handleChangeBirth(evt, "y")}
                              placeholder="YYYY"
                              maxLength={4}
                              size={4}
                              aria-required="true"
                              className="text-[18px] [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none
                              inline px-3 py-2 mr-1 border rounded w-full bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]"
                            />
                            년
                          </div>
                          <div className="text-[18px] w-3/12 flex items-center">
                            <input
                              type="number"
                              value={birth.m ?? ""}
                              onChange={(evt) => handleChangeBirth(evt, "m")}
                              placeholder="MM"
                              aria-required="true"
                              className="text-[18px] [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none
                              inline px-3 py-2 mr-1 border rounded w-full bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]"
                            />
                            월
                          </div>
                          <div className="text-[18px] w-3/12 flex items-center">
                            <input
                              type="number"
                              value={birth.d ?? ""}
                              onChange={(evt) => handleChangeBirth(evt, "d")}
                              placeholder="DD"
                              aria-required="true"
                              className="text-[18px] [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none
                              inline px-3 py-2 mr-1 border rounded w-full bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]"
                            />
                            일
                          </div>
                        </div>
                      </div>
                      {errStat.birthday && <ErrorMessage msg={errStat.birthday} />}
                    </div>
                    {/* 국적 */}
                    <div>
                      <div className="flex flex-col md:flex-row items-center">
                        <span className="md:w-1/3 font-medium">국적</span>
                        <div className="md:w-2/3 flex justify-evenly">
                          <label className="inline-flex items-center mr-4">
                            <input type="radio" className="absolute opacity-0 cursor-pointer" name="foreignFlag" checked={!data.foreignFlag} onChange={() => handleChangeForeignFlag(false)} />
                            <span className={`w-6 h-6 rounded-full border-2 transition-colors duration-300 ${data.foreignFlag === false ? "border-[#4B721F] bg-[#4B721F]" : "border-gray-300 bg-gray-50/80"} cursor-pointer`}>{data.foreignFlag === false && <span className="block w-3 h-3 rounded-full bg-white mx-auto mt-1"></span>}</span>
                            <span className="ml-2">내국인</span>
                          </label>
                          <label className="inline-flex items-center">
                            <input type="radio" className="absolute opacity-0 cursor-pointer" name="foreignFlag" checked={data.foreignFlag} onChange={() => handleChangeForeignFlag(true)} />
                            <span className={`w-6 h-6 rounded-full border-2 transition-colors duration-300 ${data.foreignFlag === true ? "border-[#4B721F] bg-[#4B721F]" : "border-gray-300 bg-gray-50/80"} cursor-pointer`}>{data.foreignFlag === true && <span className="block w-3 h-3 rounded-full bg-white mx-auto mt-1"></span>}</span>
                            <span className="ml-2">외국인</span>
                          </label>
                        </div>
                      </div>
                    </div>
                    {/* 연락처 */}
                    <div>
                      <div className="flex flex-col md:flex-row items-center">
                        <span className="md:w-1/3 font-medium">연락처</span>
                        <div className="md:w-2/3 flex justify-between items-center">
                          <div className="w-3/12 flex items-center">
                            <input
                              type="number"
                              value={phoneNumber.first ?? ""}
                              onChange={handleChangePhoneNumber("first")}
                              placeholder="010"
                              maxLength={3}
                              aria-required="true"
                              className="text-[18px] [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none
                              inline px-3 py-2 border rounded w-full bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]"
                            />
                          </div>
                          -
                          <div className="w-3/12 flex items-center">
                            <input
                              type="number"
                              value={phoneNumber.second ?? ""}
                              onChange={handleChangePhoneNumber("second")}
                              placeholder="0000"
                              maxLength={4}
                              aria-required="true"
                              className="text-[18px] [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none
                              inline px-3 py-2 border rounded w-full bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]"
                            />
                          </div>
                          -
                          <div className="w-3/12 flex items-center">
                            <input
                              type="number"
                              value={phoneNumber.third ?? ""}
                              onChange={handleChangePhoneNumber("third")}
                              placeholder="0000"
                              maxLength={4}
                              aria-required="true"
                              className="text-[18px] [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none
                              inline px-3 py-2 border rounded w-full bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]"
                            />
                          </div>
                        </div>
                      </div>
                      {errStat.phoneNumber && <ErrorMessage msg={errStat.phoneNumber} />}
                    </div>
                    {/* 주소 */}
                    <div>
                      <div className="flex flex-col md:flex-row items-center">
                        <span className="md:w-1/3 font-medium">주소</span>
                        <div className="w-full md:w-2/3 flex">
                          <button type="button" ref={addressBtnRef} className="text-[18px] flex-none hover:bg-gray-100 px-2 items-center transition-all duration-300 ease-in-out bg-gray-50 border py-2 mr-1.5 rounded" onClick={loadPostcodeSearch}>
                            찾기
                          </button>
                          <input type="text" id="address" onChange={changeAddress("address")} onClick={() => addressBtnRef.current?.click()} value={postcodeData?.roadAddress || ""} placeholder="주소를 검색해주세요." className="text-[18px] flex-grow min-w-0 px-3 py-2 border rounded bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]" readOnly />
                        </div>
                      </div>
                      {errStat.address && <ErrorMessage msg={errStat.address} />}
                      <div className="mt-3">
                        <div className="md:w-1/3 inline-block" />
                        <input type="text" id="addressDetail" value={detailedAddress} onChange={(evt) => setDetailAddress(evt.target.value)} placeholder="상세주소를 입력해주세요." className="text-[18px] w-full md:w-2/3 px-3 py-2 border rounded bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]" />
                      </div>
                      {errStat.detailedAddress && <ErrorMessage msg={errStat.detailedAddress} />}
                    </div>

                    <div className="flex justify-between">
                      <button type="button" onClick={() => setStep(1)} className="w-[48%] bg-gray-300 text-[22px] text-black py-3 rounded hover:bg-gray-400">
                        이전
                      </button>
                      <button type="submit" className="w-[48%] bg-[#4B721F] text-[22px] text-white py-3 rounded hover:bg-[#3E5A1E]">
                        회원가입하기
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </div>
          )}

          <div className="rounded-r-[20px] order-1 md:order-2">
            <img src={StopTurtleImg} className={isMobile ? "rounded-t-[20px] w-full h-[300px] object-none" : "rounded-r-[20px] h-full object-cover"} />
          </div>
        </div>
      </div>
    </>
  );
}

export default JoinPage;
