import { create } from "zustand";
import { MetaMaskSDK } from "@metamask/sdk";
import Web3 from "web3";
import { Contract } from "web3-eth-contract";
import { AbiItem } from "web3-utils";
import TurtleTokenAbi from "../abi/TurtleToken.json";

const TURTLE_TOKEN_ABI: AbiItem[] = TurtleTokenAbi.abi as AbiItem[];
const TURTLE_TOKEN_ADDRESS = "0x73b0697A8A69193e5842497Cc010cB1F34A93a0a";

interface MetaMaskSDKState {
  MMSDK: MetaMaskSDK | null;
  web3: Web3 | null;
  account: string;
  contract: Contract<typeof TURTLE_TOKEN_ABI> | null;
  error: string | null;
  isInitialized: boolean;
  initializeSDK: () => Promise<void>;
  connectWallet: () => Promise<void>;
  handleAccountsChanged: (accounts: string[]) => void;
}

export const useMetaMaskSDKStore = create<MetaMaskSDKState>((set, get) => ({
  MMSDK: null,
  web3: null,
  account: "",
  contract: null,
  error: null,
  isInitialized: false,

  initializeSDK: async () => {
    if (get().isInitialized) return;

    try {
      const MMSDK = new MetaMaskSDK({
        dappMetadata: {
          name: "KkobukZIP",
          url: window.location.href,
        },
      });

      await MMSDK.init();

      const provider = MMSDK.getProvider()!;
      const web3Instance = new Web3(provider);

      const tokenContract = new web3Instance.eth.Contract(
        TURTLE_TOKEN_ABI,
        TURTLE_TOKEN_ADDRESS
      ) as unknown as Contract<typeof TURTLE_TOKEN_ABI>;

      const accounts = await web3Instance.eth.getAccounts();
      const account = accounts.length > 0 ? accounts[0] : "";

      // 계정 변경 이벤트 리스너 설정
      provider.on('accountsChanged', (...args: unknown[]) => {
        const accounts = args[0] as string[];
        get().handleAccountsChanged(accounts);
      });

      set({
        MMSDK,
        web3: web3Instance,
        contract: tokenContract,
        account,
        isInitialized: true,
        error: null,
      });
    } catch (error) {
      console.error("Failed to initialize MetaMask SDK:", error);
      set({ error: "MetaMask 초기화에 실패했습니다." });
    }
  },

  connectWallet: async () => {
    const { MMSDK } = get();
    if (!MMSDK) {
      set({ error: "MetaMask SDK가 초기화되지 않았습니다." });
      return;
    }

    try {
      const accounts = await MMSDK.connect();
      if (accounts.length > 0) {
        set({ account: accounts[0], error: null });
      }
    } catch (error) {
      console.error("Failed to connect wallet:", error);
      set({ error: "지갑 연결에 실패했습니다." });
    }
  },

  handleAccountsChanged: (accounts: string[]) => {
    if (accounts.length === 0) {
      // MetaMask is locked or the user has not connected any accounts
      set({ account: "", error: "MetaMask가 잠겨있거나 계정이 연결되지 않았습니다." });
    } else if (accounts[0] !== get().account) {
      // Update the current account
      set({ account: accounts[0], error: null });
    }
  },
}));