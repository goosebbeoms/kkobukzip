import TurtleListLayout from "../../layout/TurtleListLayout";
import AuctionTurtle from "../../components/auction/AuctionTurtle";
import { getAuctionDatas } from "../../apis/tradeApi";
import { useCallback, useState } from "react";
import { AuctionItemDataType } from "../../types/auction";

const AuctionListPage = () => {
  const [AuctionItems, setAuctionItems] = useState<JSX.Element[]>([]);
  const [ProgressAuctionItems, setProgressAuctionItems] = useState<
    JSX.Element[]
  >([]);
  const [isProgressItem, setIsProgressItem] = useState(false);

  const fetchData = useCallback(
    async (page: number, filters: object, isSearch?: boolean) => {
      
      const result = await getAuctionDatas({ page, ...filters });
      console.log(result.data.data.data.auctions);

      if(result.status == 200){
        const progressItems: JSX.Element[] = [];
        const auctionItems = result.data.data.data.auctions.map(
          (item: AuctionItemDataType) => {
            if(item.progress == "DURING_AUCTION")
              progressItems.push(
                <AuctionTurtle key={item.id} data={item}/>
              );
            
            return <AuctionTurtle key={item.id} data={item}/>;
          }
        );
        if(isSearch){
          setAuctionItems(() => [...auctionItems]);
          setProgressAuctionItems(() => [...progressItems]);
        } else {
          setAuctionItems((prev) => [...prev, ...auctionItems]);
          setProgressAuctionItems((prev) => [...prev, ...progressItems]);
        }
        return result;
      }
    },
    []
  );

  const progressFilter = () => {
    setIsProgressItem(!isProgressItem);
  };

  return (
    <TurtleListLayout
      title="경매중인 거북이"
      items={isProgressItem ? ProgressAuctionItems : AuctionItems}
      fetchData={fetchData}
      isProgressItemChecked={isProgressItem}
      setIsProgressItemChecked={progressFilter}
    />
  );
};

export default AuctionListPage;
