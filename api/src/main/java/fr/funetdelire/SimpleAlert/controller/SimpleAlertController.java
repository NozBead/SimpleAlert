package fr.funetdelire.SimpleAlert.controller;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alert")
public class SimpleAlertController {
	@Autowired
	LinkedList<String> alertQueue;

	@GetMapping
	public  ResponseEntity<String> getAlert() {
		ResponseEntity<String> response;
		if (alertQueue.isEmpty()) {
			response = ResponseEntity.notFound().build();
		}
		else {
			response = ResponseEntity.ok(alertQueue.poll());
		}
		return response;
	}
}
