import { useState } from "react";
import { Helmet } from "react-helmet-async";
import { Link, useNavigate } from "react-router-dom";
import BackgroundImg from "../../assets/Side_View_Scene.gif";
import BackgroundImage from "../../assets/login_background.jpg";
import naver_logo from "../../assets/login/naver_logo2.png";
import kakao_logo from "../../assets/login/kakao_logo.png";
import Header from "../../components/common/Header";
import StopTurtleImg from "../../assets/turtle_home_stop.png";
import { FaRegEye, FaRegEyeSlash } from "react-icons/fa6";

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [hide, setHide] = useState(true);

  const navigate = useNavigate();

  const handleLogin = () => {
    alert("로그인 핸들");
    console.log(email, password);
    navigate("/login");
  };

  return (
    <>
      <Helmet>
        <title>로그인</title>
      </Helmet>
      <Header/>
      {/* <img
        src={BackgroundImg}
        className="w-full h-full object-cover min-h-screen"
        draggable="false"
      /> */}
      <div className="absolute top-0 left-0 right-0 px-[250px] mt-[85px] flex justify-center items-center" style={{ height: 'calc(100vh - 85px)' }}>


      {/* 로그인 컴포넌트 */}
      <div className="relative w-full h-[570px] bg-[#D5E5BD] backdrop-blur-sm rounded-[20px] shadow-[20px] z-10 flex flex-row">
          <div className="rounded-l-[20px] w-1/2">
            <img src={StopTurtleImg} className="rounded-l-[20px] h-full object-cover"/>
          </div>
          
          <div className="w-1/2 h-full rounded-r-[20px] m-auto flex justify-center items-center">
            <div className="w-2/3">
              <h2 className="text-[38px] text-center mb-8 font-dnf-bitbit">로그인</h2>

              <form className="space-y-5 mb-10">
                <input
                  onChange={(e) => {
                    setEmail(e.target.value);
                  }}
                  type="email"
                  placeholder="이메일"
                  className="w-full p-3 text-[20px] border rounded bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]"
                />
                <div className="relative">
                  <input
                  onChange={(e) => {
                    setPassword(e.target.value);
                  }}
                  type={hide ? 'password' : 'text'}
                  placeholder="비밀번호"
                  className="w-full p-3 text-[20px] border rounded bg-gray-50/80 focus:outline-none focus:ring-2 focus:ring-[#4B721F]"
                  />
                  {
                    hide ?
                      <div className="cursor-pointer absolute top-[17px] right-5 text-[21px] text-[#7c7c7c]" onClick={() => setHide(false)}><FaRegEyeSlash /></div>
                      : <div className="cursor-pointer absolute top-[17px] right-5 text-[21px] text-[#7c7c7c]" onClick={() => setHide(true)}><FaRegEye /></div>
                  }
                </div>
                
                <button
                  onClick={handleLogin}
                  className="w-full bg-[#4B721F] text-[22px] text-white py-3 rounded hover:bg-[#3E5A1E]"
                >
                  로그인하기
                </button>
              </form>

              {/* <div className="flex justify-between items-center mt-4 text-[17px] font-bold">
                <span>아직 회원이 아니신가요?</span>
                <Link to="/join" className="text-blue-500 hover:underline">
                  회원가입
                </Link>
              </div> */}

              <div className="flex flex-row justify-center gap-12">
                  <div className="cursor-pointer w-[53px] h-[53px] bg-[#03C75A] rounded-full flex justify-center items-center">
                  <img
                    src={naver_logo}
                    alt="Naver Logo"
                    className="w-6 h-6"
                  />

                  </div>

                  <div className="cursor-pointer w-[53px] h-[53px] bg-[#fae300] rounded-full flex justify-center items-center">
                  <img
                    src={kakao_logo}
                    alt="Kakao Logo"
                    className="w-6 h-6"
                  />

                  </div>
              </div>
            </div>
            

            
          </div>
        </div>
      </div>
    </>
  );
}

export default LoginPage;
