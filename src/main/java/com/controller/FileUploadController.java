package com.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.model.FileModel;
import com.repository.Repository;

@RestController
public class FileUploadController {

	@Autowired
	private Repository repository;
	
	 private final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

	    //Save the uploaded file to this folder
	    private static String UPLOADED_FOLDER = "D://temp//";
	   
	    // 3.1.3 maps html form to a Model
	    @PostMapping("/api/upload/multi/model")
	    public ResponseEntity<?> multiUploadFileModel(@ModelAttribute FileModel model,
	    		@RequestParam("file") MultipartFile[] uploadfile) {
	        logger.debug("Upload file With metadata");
	        try {	        	
	            saveUploadedFiles(Arrays.asList(uploadfile), model);
	        } catch (IOException e) {
				logger.error("Error while uploading file", e);
	            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	        }
	        return new ResponseEntity<Object>("Successfully uploaded!", HttpStatus.OK);
	    }
	    
	    @GetMapping("/api/download/{id}")
	    @ResponseBody
	    public ResponseEntity<Resource> serveFile(@PathVariable(value="id") long id) {
	        logger.debug("Download file With id");
	    	FileModel fileModel = repository.findOne(id);
	    	Path filePath = Paths.get(UPLOADED_FOLDER + "//" + fileModel.getFileType() +
    	            "//" + fileModel.getId() + "//" +fileModel.getFileOriginalName());	    
            Resource file = null;;
			try {
				file = new UrlResource(filePath.toUri());
			} catch (MalformedURLException e) {
				logger.error("Error while downlaoading file", e);
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
            return ResponseEntity
	                .ok()
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+fileModel.getFileOriginalName()+"\"")
	                .body(file);
	    }
	    
	    @GetMapping("/api/listFiles")
	     public Iterable<FileModel> listUploadedFiles() {
	    	return  repository.findAll();	    	
	    }

	    //save file
	    private void saveUploadedFiles(List<MultipartFile> files, FileModel model) throws IOException {
	        for (MultipartFile file : files) {
	            if (file.isEmpty()) {
	                continue; //next pls
	            }
	            byte[] bytes = file.getBytes();
	            Path createPath = Paths.get(UPLOADED_FOLDER + "//" + model.getFileType() +
	            "//" + model.getId());
	            Files.createDirectories(createPath);
	            Path path = Paths.get(UPLOADED_FOLDER + "//" + model.getFileType() +
	    	            "//" + model.getId() + "//" +file.getOriginalFilename());	            
	            Files.write(path, bytes);
	            model.setFileOriginalName(file.getOriginalFilename());
	            repository.save(model);
	        }
	    }	  
	}