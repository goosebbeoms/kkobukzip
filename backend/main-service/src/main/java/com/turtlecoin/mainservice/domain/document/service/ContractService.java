package com.turtlecoin.mainservice.domain.document.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import com.turtlecoin.mainservice.domain.document.entity.contract.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import com.turtlecoin.mainservice.domain.turtle.entity.Gender;
import com.turtlecoin.mainservice.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContractService {
	private final Web3j web3j;
	private final Credentials credentials;
	private final ContractGasProvider contractGasProvider;
	// smartcontract의 주소
	private final String contractAddress = "0x4b5d7E32aBD87278ECDE86013a60Ea522679984a";

	@Autowired
	private UserService userService;

	@Autowired
	public ContractService(Web3j web3j) {
		this.web3j = web3j;
		// 관리 계정의 private key
		this.credentials = Credentials.create("0xca2e77682f34e3968a551c668edc6d7568460bb299af16fbc76e8b54537aa142"); // 개인 키

		// 가스 가격과 가스 한도 설정
		BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L); // 20 Gwei
		BigInteger gasLimit = BigInteger.valueOf(6721975); // 300,000

		// StaticGasProvider를 이용한 설정
		StaticGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);

		this.contractGasProvider = gasProvider;
	}

	// 스마트 컨트랙트 로드
	public TurtleDocumentation loadTurtleDocumentationContract() {
		return TurtleDocumentation.load(contractAddress, web3j, credentials, contractGasProvider);
	}

	// 인공증식서류 등록
	public String registerTurtleMultiplicationDocument(
		String turtleUUID, String applicant, String documentHash, BigInteger count, String area,
		String purpose,	String location, String fatherUUID, String motherUUID,
		LocalDate birth, String name, int weight, Gender gender,
		String locationSpecification, String multiplicationMethod, String shelterSpecification) throws Exception {

		TurtleDocumentation turtleDocumentation = loadTurtleDocumentationContract();
		// byte로 변환
		byte[] byteArray = hexStringToByte32("0x" + documentHash);
		TransactionReceipt receipt =  turtleDocumentation.registerTurtleMultiplicationDocument(
			turtleUUID, applicant, byteArray, count, area, purpose, location, fatherUUID, motherUUID,
			birth.toString(), name, BigInteger.valueOf(weight), gender.toString(),
			locationSpecification, multiplicationMethod, shelterSpecification
		).send();

		// 이벤트 호출
		List<TurtleDocumentation.TurtleMultiplicationEventResponse> events = TurtleDocumentation.getTurtleMultiplicationEvents(receipt);

		byte[] returnDocumentHash = null;
		returnDocumentHash = events.get(0).documentHash;

		return byteToString(returnDocumentHash);
	}

	// 인공증식서류 조회
	public TurtleDocumentation.Multiplication searchTurtleMultiplicationDocument(String turtleUUID, String documentHash) throws Exception {
		TurtleDocumentation turtleDocumentation = loadTurtleDocumentationContract();
		//byte[] byteArray = toByte32(hexStringToByteArray(documentHash));
		byte[] byteArray = hexStringToByte32("0x" + documentHash);

		return turtleDocumentation.searchTurtleMultiplicationDocument(turtleUUID, byteArray).send();
	}

	// 양수서류 등록
	public String registerTurtleAssigneeDocument(
		String turtleUUID, String applicant, String documentHash, String assigneeID,
		BigInteger count, String transferReason, String purpose
	) throws Exception {
		TurtleDocumentation turtleDocumentation = loadTurtleDocumentationContract();
		// byte로 변환
		byte[] byteArray = hexStringToByte32("0x" + documentHash);
		TransactionReceipt receipt = turtleDocumentation.registerTurtleAssigneeDocument(
			turtleUUID, applicant, byteArray, assigneeID, count, transferReason, purpose
		).send();

		// 이벤트 호출
		List<TurtleDocumentation.TurtleTransferredEventResponse> events = TurtleDocumentation.getTurtleTransferredEvents(receipt);

		byte[] returnDocumentHash = null;
		returnDocumentHash = events.get(0).documentHash;

		return byteToString(returnDocumentHash);
	}

	// 양도서류 등록
	public String registerTurtleGrantorDocument(
		String turtleUUID, String applicant, String documentHash, String grantorID,
		String aquisition, String fatherUUID, String motherUUID
	) throws Exception {
		// byte로 변환
		byte[] byteArray = hexStringToByte32("0x" + documentHash);
		TurtleDocumentation turtleDocumentation = loadTurtleDocumentationContract();
		TransactionReceipt receipt = turtleDocumentation.registerTurtleGrantorDocument(
			turtleUUID, applicant, byteArray, grantorID, aquisition, fatherUUID, motherUUID
		).send();

		// 이벤트 호출
		List<TurtleDocumentation.TurtleTransferredEventResponse> events = TurtleDocumentation.getTurtleTransferredEvents(receipt);

		byte[] returnDocumentHash = null;
		returnDocumentHash = events.get(0).documentHash;

		return byteToString(returnDocumentHash);
	}

	// 양도양수서류 조회
	public TurtleDocumentation.Transfer searchTurtleTransferDocument(String turtleUUID, String documentHash) throws Exception {
		TurtleDocumentation turtleDocumentation = loadTurtleDocumentationContract();
		byte[] byteArray = hexStringToByte32("0x" + documentHash);
		return turtleDocumentation.searchTurtleTransferDocument(turtleUUID, byteArray).send();
	}

	// 폐사질병서류 등록
	public String registerTurtleDeathDocument(
		String turtleUUID, String applicant, String documentHash, String shelter,BigInteger count,
		String deathReason, String plan, String deathImage, String diagnosis
	) throws Exception {
		TurtleDocumentation turtleDocumentation = loadTurtleDocumentationContract();
		// byte로 변환
		byte[] byteArray = hexStringToByte32("0x" + documentHash);
		TransactionReceipt receipt = turtleDocumentation.registerTurtleDeathDocument(
			turtleUUID, applicant, byteArray, shelter, count, deathReason, plan, deathImage, diagnosis
		).send();

		// 이벤트 호출
		List<TurtleDocumentation.TurtleDeathEventResponse> events = TurtleDocumentation.getTurtleDeathEvents(receipt);

		byte[] returnDocumentHash = null;
		returnDocumentHash = events.get(0).documentHash;

		return byteToString(returnDocumentHash);
	}

	// 폐사질병서류 조회
	public TurtleDocumentation.Death searchTurtleDeathDocument(String turtleUUID, String documentHash) throws Exception {
		TurtleDocumentation turtleDocumentation = loadTurtleDocumentationContract();
		byte[] byteArray = hexStringToByte32("0x" + documentHash);
		return turtleDocumentation.searchTurtleDeathDocument(turtleUUID, byteArray).send();
	}

	// 가장 최근 서류 조회
	public String searchCurrentDocumentHash(String turtleUUID) throws Exception {
		TurtleDocumentation turtleDocumentation = loadTurtleDocumentationContract();

		byte[] documentHash = turtleDocumentation.searchCurrentDocumentHash(turtleUUID).send();
		return byteToString(documentHash);
	}

	// 인공증식서류 승인
	public void approveBreeding(String turtleUUID, String documentHash) throws Exception {
		TurtleDocumentation turtleDocumentation = loadTurtleDocumentationContract();
		byte[] byteArray = hexStringToByte32("0x" + documentHash);
		turtleDocumentation.approveMultiplicationDocByReviewer(turtleUUID, byteArray).send();
	}

	// 양도양수 승인
	public void approveTransfer(String turtleUUID, String documentHash) throws Exception {
		TurtleDocumentation turtleDocumentation = loadTurtleDocumentationContract();
		byte[] byteArray = hexStringToByte32("0x" + documentHash);
		turtleDocumentation.approveTransferDocByReviewer(turtleUUID, byteArray).send();
	}

	// byte32로 변환하기
	public byte[] hexStringToByte32(String hex) {
		if (hex.startsWith("0x")) {
			hex = hex.substring(2);  // '0x' 제거
		}
		byte[] byteArray = new byte[32];
		int length = Math.min(hex.length() / 2, 32);

		for (int i = 0; i < length; i++) {
			byteArray[i] = (byte) ((Character.digit(hex.charAt(i * 2), 16) << 4)
				+ Character.digit(hex.charAt(i * 2 + 1), 16));
		}
		return byteArray;
	}

	// byte[] -> String
	public String byteToString(byte[] bytes){
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			// %02X는 대문자 Hex를 생성합니다.
			hexString.append(String.format("%02X", b));
		}
		return hexString.toString();
	}

	// keccak256 생성하기
	public String keccak256(byte[] input) {
		return byteToString(Hash.sha3(input)); // Keccak-256 해시 계산
	}
}