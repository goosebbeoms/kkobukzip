import { useState } from "react";

function AuctionItemInfo({ images }: { images: string[] }) {
  const [currentIndex, setCurrentIndex] = useState(0);

  const handleNext = () => {
    setCurrentIndex((prevIndex) => (prevIndex + 1) % images.length);
  };

  const handlePrev = () => {
    setCurrentIndex((prevIndex) =>
      prevIndex === 0 ? images.length - 1 : prevIndex - 1
    );
  };

  return (
    <>
      <div className="w-[48%] h-[360px] rounded-[20px] relative">
        <img
          src={images[currentIndex]}
          className="w-full h-full object-cover rounded-[20px]"
          alt="Turtle"
          draggable="false"
        />
        <button
          onClick={handlePrev}
          className="absolute left-0 top-1/2 transform -translate-y-1/2 text-white text-[27px] p-2 rounded-full font-bold"
        >
          &lt;
        </button>
        <button
          onClick={handleNext}
          className="absolute right-0 top-1/2 transform -translate-y-1/2 text-white text-[27px] p-2 rounded-full font-bold"
        >
          &gt;
        </button>
        <div className="absolute bottom-3 right-3 bg-gray-700 text-white px-4 py-2 rounded-[20px]">
          {currentIndex + 1} / {images.length}
        </div>
        <div className="flex flex-row justify-between items-center mt-[15px] mb-[7px]">
          <div className="text-[23px]">페닐슐라쿠터</div>
          <div className="flex flex-row space-x-2">
            <span className="px-2 py-1 rounded-full cursor-pointer text-[18px] bg-[#D5F0DD] text-[#065F46]">
              #암컷
            </span>
            <span className="px-2 py-1 rounded-full cursor-pointer text-[18px] bg-[#D5F0DD] text-[#065F46]">
              #베이비
            </span>
          </div>
        </div>
        <div className="mb-[13px] text-[#9A9A9A] text-[17px]">
          24년 8월 10일생 | 8kg
        </div>
        <div className="text-[17px] leading-7 border-[2px] rounded-[10px] p-2 line-clamp">
          이 붉은귀거북은 활발하고 건강한 상태로, 밝고 선명한 붉은색 귀 무늬가
          특징입니다. 현재까지 특별한 질병 없이 건강하게 자라왔으며, 균형 잡힌
          사료와 신선한 채소로 영양 관리가 잘 되어 있습니다. 특히 수영을
          좋아하며, 물속에서의 활동이 활발해 관찰하는 재미가 큽니다. 이 거북이는
          비교적 온순한 성격을 가지고 있어 손을 자주 타지는 않지만, 스트레스를
          주지 않는 선에서 손길을 허용하는 편입니다. 호기심이 많아 주변 환경에
          대한 관심을 보이는 등, 관상용으로도 매력적인 개체입니다.
        </div>
        <div className="mt-[20px] mb-[3px] text-[#737373] font-bold">
          판매자 정보
        </div>
        <div className="bg-[#F2F2F2] h-[60px] rounded-[10px] flex flex-row justify-between items-center px-2 py-1">
          <div className="flex flex-row items-center">
            <img
              //   src={tmpProfileImg}
              className="rounded-full w-[43px] h-[43px] mr-3"
              draggable="false"
            />
            <span className="text-[20px]">꼬북맘</span>
          </div>
          <div className="cursor-pointer bg-[#7CBBF9] h-fit flex justify-center items-center rounded-[10px] font-bold px-3 py-2 text-white">
            채팅하기
          </div>
        </div>
      </div>
    </>
  );
}

export default AuctionItemInfo;