package com.turtlecoin.mainservice.domain.document.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.turtlecoin.mainservice.domain.document.dto.AssignDocumentRequest;
import com.turtlecoin.mainservice.domain.document.dto.BreedingDocumentRequest;
import com.turtlecoin.mainservice.domain.document.dto.DeathDocumentRequest;
import com.turtlecoin.mainservice.domain.document.dto.DocumentListDto;
import com.turtlecoin.mainservice.domain.document.dto.GrantDocumentRequest;
import com.turtlecoin.mainservice.domain.document.dto.TempDto;
import com.turtlecoin.mainservice.domain.document.entity.DocType;
import com.turtlecoin.mainservice.domain.document.entity.Document;
import com.turtlecoin.mainservice.domain.document.entity.Progress;
import com.turtlecoin.mainservice.domain.document.service.DocumentService;
import com.turtlecoin.mainservice.domain.s3.service.ImageUploadService;
import com.turtlecoin.mainservice.global.ResponseVO;

import lombok.RequiredArgsConstructor;

// 1. Solidity가 완성되면 블록체인 저장 / 조회 부분을 넣어줘야 함
// 2. Security가 완성되면 user 정보를 가져와서 저장하는 부분을 넣어줘야 함

// 3. 수정 부분 어떻게 할 건지 결정해야 됨
// 특히 양도 양수는 하나의 서류를 가지고 시도할 텐데 이거 고려해서 다시 짜봐..

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main/document")
public class DocumentController {
	private final ImageUploadService imageUploadService;
	private final DocumentService documentService;

	// 인공증식서류 등록
	@PostMapping(value = "/register/breed", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<?> registerBreedingDocument(
		@RequestPart("data") BreedingDocumentRequest requestData,
		@RequestPart("locationSpecification") MultipartFile locationSpecification,
		@RequestPart("multiplicationMethod") MultipartFile multiplicationMethod,
		@RequestPart("shelterSpecification") MultipartFile shelterSpecification) {

		String locationSpecificationAddress = "";
		String multiplicationAddress = "";
		String shelterSpecificationAddress = "";

		// S3에 이미지를 업로드
		try{
			locationSpecificationAddress = imageUploadService.upload(locationSpecification, "breedDocument");
			multiplicationAddress = imageUploadService.upload(multiplicationMethod, "breedDocument");
			shelterSpecificationAddress = imageUploadService.upload(shelterSpecification, "breedDocument");
		}catch (Exception e){
			//e.printStackTrace();
			// 로직중 에러가 발생하면 모두 처음으로 복구시킨다.
			imageUploadService.deleteS3(locationSpecificationAddress);
			imageUploadService.deleteS3(multiplicationAddress);
			imageUploadService.deleteS3(shelterSpecificationAddress);

			return new ResponseEntity<>(ResponseVO.failure("서류 등록에 실패했습니다.", e.getMessage()), HttpStatus.BAD_REQUEST);
		}

		String hash = "";
		String turtleUUID = "";
		// 블록체인에 업로드 ( 미구현 )
		try{
			hash = UUID.randomUUID().toString();
			turtleUUID = UUID.randomUUID().toString();
		}
		catch(Exception e){
			//e.printStackTrace();
			// 로직중 에러가 발생하면 모두 처음으로 복구시킨다.
			imageUploadService.deleteS3(locationSpecificationAddress);
			imageUploadService.deleteS3(multiplicationAddress);
			imageUploadService.deleteS3(shelterSpecificationAddress);
			return new ResponseEntity<>(ResponseVO.failure("서류 등록에 실패했습니다.", e.getMessage()), HttpStatus.BAD_REQUEST);
		}

		// DB에 최종적으로 데이터 저장
		try{
			Document document = Document.builder()
				.documentHash(hash)
				.progress(Progress.DOCUMENT_REVIEWING)
				.turtleUUID(turtleUUID)
				.docType(DocType.BREEDING)
				.build();
			documentService.save(document);
		}catch(Exception e){
			// 에러가 발생했을 경우 DB에 null 인 상태로라도 저장해야 함
			Document document = Document.builder()
				.documentHash(null)
				.progress(Progress.DOCUMENT_REVIEWING)
				.turtleUUID(turtleUUID)
				.docType(DocType.BREEDING)
				/*
							유저 서비스 구현되면 꼭 추가 해줘야 함!!!!!!!!!!!!!!!!!!!!!!1
				 */
				.applicant("dump")
				.build();
			documentService.save(document);

			// 블록체인에 저장된 거니까 등록에는 성공한거다
			return new ResponseEntity<>(ResponseVO.success("서류 등록에 성공했습니다."), HttpStatus.OK);
		}

		// 응답
		return new ResponseEntity<>(ResponseVO.success("서류 등록에 성공했습니다."), HttpStatus.OK);
	}

	// 양도신청서 등록
	@PostMapping(value = "/register/grant", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<?> registerGrantDocument(@RequestBody GrantDocumentRequest requestData) {
		String hash = "";
		String turtleUUID = "";
		// 블록체인에 업로드 ( 미구현 )
		try{
			hash = UUID.randomUUID().toString();
			turtleUUID = UUID.randomUUID().toString();
		}
		catch(Exception e){
			//e.printStackTrace();
			return new ResponseEntity<>(ResponseVO.failure("서류 등록에 실패했습니다.", e.getMessage()), HttpStatus.BAD_REQUEST);
		}

		// DB에 최종적으로 데이터 저장
		try{
			Document document = Document.builder()
				.documentHash(hash)
				.progress(Progress.DOCUMENT_REVIEWING)
				.turtleUUID(turtleUUID)
				.docType(DocType.TRANSFER)
				/*
							유저 서비스 구현되면 꼭 추가 해줘야 함!!!!!!!!!!!!!!!!!!!!!!1
				 */
				.applicant("dump")
				.build();
			documentService.save(document);
		}catch(Exception e){
			// 에러가 발생했을 경우 DB에 null 인 상태로라도 저장해야 함
			Document document = Document.builder()
				.documentHash(null)
				.progress(Progress.DOCUMENT_REVIEWING)
				.turtleUUID(turtleUUID)
				.docType(DocType.TRANSFER)
				/*
							유저 서비스 구현되면 꼭 추가 해줘야 함!!!!!!!!!!!!!!!!!!!!!!1
				 */
				.applicant("dump")
				.build();
			documentService.save(document);

			// 블록체인에 저장된 거니까 등록에는 성공한거다
			return new ResponseEntity<>(ResponseVO.success("서류 등록에 성공했습니다."), HttpStatus.OK);
		}

		// 응답
		return new ResponseEntity<>(ResponseVO.success("서류 등록에 성공했습니다."), HttpStatus.OK);
	}

	// 양수신청서 등록
	@PostMapping(value = "/register/assign", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<?> registerAssignDocument(@RequestBody AssignDocumentRequest requestData) {
		String hash = "";
		String turtleUUID = "";
		// 블록체인에 업로드 ( 미구현 )
		try{
			hash = UUID.randomUUID().toString();
			turtleUUID = UUID.randomUUID().toString();
		}
		catch(Exception e){
			//e.printStackTrace();
			return new ResponseEntity<>(ResponseVO.failure("서류 등록에 실패했습니다.", e.getMessage()), HttpStatus.BAD_REQUEST);
		}

		// DB에 최종적으로 데이터 저장
		try{
			Document document = Document.builder()
				.documentHash(hash)
				.progress(Progress.DOCUMENT_REVIEWING)
				.turtleUUID(turtleUUID)
				.docType(DocType.TRANSFER)
				/*
							유저 서비스 구현되면 꼭 추가 해줘야 함!!!!!!!!!!!!!!!!!!!!!!1
				 */
				.applicant("dump")
				.build();
			documentService.save(document);
		}catch(Exception e){
			// 에러가 발생했을 경우 DB에 null 인 상태로라도 저장해야 함
			Document document = Document.builder()
				.documentHash(null)
				.progress(Progress.DOCUMENT_REVIEWING)
				.turtleUUID(turtleUUID)
				.docType(DocType.TRANSFER)
				.build();
			documentService.save(document);

			// 블록체인에 저장된 거니까 등록에는 성공한거다
			return new ResponseEntity<>(ResponseVO.success("서류 등록에 성공했습니다."), HttpStatus.OK);
		}

		// 응답
		return new ResponseEntity<>(ResponseVO.success("서류 등록에 성공했습니다."), HttpStatus.OK);
	}

	// 폐사질병서류 등록
	@PostMapping(value = "/register/death", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<?> registerDeathDocument(
		@RequestPart("data") DeathDocumentRequest requestData,
		@RequestPart("deathImage") MultipartFile deathImage,
		@RequestPart("diagnosis") MultipartFile diagnosis) {

		String deathImageAddress = "";
		String diagnosisAddress = "";

		// S3에 이미지를 업로드
		try{
			deathImageAddress = imageUploadService.upload(deathImage, "deathDocument");
			diagnosisAddress = imageUploadService.upload(diagnosis, "deathDocument");}
		catch (Exception e){
			//e.printStackTrace();
			// 로직중 에러가 발생하면 모두 처음으로 복구시킨다.
			imageUploadService.deleteS3(deathImageAddress);
			imageUploadService.deleteS3(diagnosisAddress);

			return new ResponseEntity<>(ResponseVO.failure("서류 등록에 실패했습니다.", e.getMessage()), HttpStatus.BAD_REQUEST);
		}

		String hash = "";
		String turtleUUID = "";
		// 블록체인에 업로드 ( 미구현 )
		try{
			hash = UUID.randomUUID().toString();
			turtleUUID = UUID.randomUUID().toString();
		}
		catch(Exception e){
			//e.printStackTrace();
			// 로직중 에러가 발생하면 모두 처음으로 복구시킨다.
			imageUploadService.deleteS3(deathImageAddress);
			imageUploadService.deleteS3(diagnosisAddress);
			return new ResponseEntity<>(ResponseVO.failure("서류 등록에 실패했습니다.", e.getMessage()), HttpStatus.BAD_REQUEST);
		}

		// DB에 최종적으로 데이터 저장
		try{
			Document document = Document.builder()
				.documentHash(hash)
				.progress(Progress.DOCUMENT_REVIEWING)
				.turtleUUID(turtleUUID)
				.docType(DocType.DEATH)
				/*
							유저 서비스 구현되면 꼭 추가 해줘야 함!!!!!!!!!!!!!!!!!!!!!!1
				 */
				.applicant("dump")
				.build();
			documentService.save(document);
		}catch(Exception e){
			// 에러가 발생했을 경우 DB에 null 인 상태로라도 저장해야 함
			Document document = Document.builder()
				.documentHash(null)
				.progress(Progress.DOCUMENT_REVIEWING)
				.turtleUUID(turtleUUID)
				.docType(DocType.DEATH)
				.build();
			documentService.save(document);

			// 블록체인에 저장된 거니까 등록에는 성공한거다
			return new ResponseEntity<>(ResponseVO.success("서류 등록에 성공했습니다."), HttpStatus.OK);
		}

		// 응답
		return new ResponseEntity<>(ResponseVO.success("서류 등록에 성공했습니다."), HttpStatus.OK);
	}

	@GetMapping("/list")
	public ResponseEntity<?> listDocuments() {
		List<DocumentListDto> documentList;

		try{
			// 관리자가 확인 중인 서류를 모두 조회
			documentList = documentService.getDocumentList();
		}catch(Exception e){
			return new ResponseEntity<>(ResponseVO.failure("서류 등록에 실패했습니다.", e.getMessage()), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(ResponseVO.success("data", documentList), HttpStatus.OK);
	}
	
	// 서류 상세 조회
	@GetMapping("/{turtleUUID}/{documentHash}")
	public ResponseEntity<?> getDocument(
		@PathVariable(value = "turtleUUID") String turtleUUID,
		@PathVariable(value = "documentHash") String documentHash) {

		TempDto tempDto = documentService.getDocument(documentHash, turtleUUID);

		return new ResponseEntity<>(ResponseVO.success("메시지", tempDto), HttpStatus.OK);
	}
}
