package com.example.demo.controllers;

import com.example.demo.api.PersonsApi;
import com.example.demo.converters.AddressConverter;
import com.example.demo.converters.DocumentConverter;
import com.example.demo.converters.PersonsConverter;
import com.example.demo.converters.PhoneConverter;
import com.example.demo.models.*;
import com.example.demo.models.exceptions.PersonNotExistsException;
import com.example.demo.services.interfaces.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PersonsController implements PersonsApi {

    private final PersonsService personsService;
    private final DocumentService documentService;
    private final PhoneService phoneService;
    private final AddressService addressService;
    private final RelationshipService relationshipService;

    @Override
    public ResponseEntity<List<Person>> getAllPersons() {
        var persons = personsService.getAllPersons();

        return ResponseEntity.ok(persons.stream().map(PersonsConverter::convertToPersonDto)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<Person> postPerson(final Person body) {
        var person = personsService.postPerson(PersonsConverter.convertToEntity(body));

        return ResponseEntity.created(URI.create("/personas/" + person.getId())).body(body);
    }

    @Override
    public ResponseEntity<Void> deletePersonById(final Long userId) {

        try{
            personsService.deletePersonById(userId);
        }catch (PersonNotExistsException e){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Person> getPersonById(final Long userId) {
        var person = personsService.getPersonById(userId)
                .map(PersonsConverter::convertToPersonDto);

        return person
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Person> updatePersonById(final Long userId, final Person body) {
        var person = personsService.updatePersonById(userId,
                PersonsConverter.convertToEntity(body));
        return ResponseEntity.ok(PersonsConverter.convertToPersonDto(person));
    }

    @Override
    public ResponseEntity<Document> getDocumentByUserId(Long userId) {
        var document = documentService.getDocumentByUserId(userId)
                .map(DocumentConverter::convertToDocumentDto);

        return document
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> setParentRelation(final Long parentId, final Long childId) {
        relationshipService.setParentRelation(parentId, childId);

        return ResponseEntity.created(URI.create("/personas/"+parentId+"/padre"+childId)).build();
    }

    @Override
    public ResponseEntity<ParentRelation> getParentRelation(final Long userId) {
        var parent = personsService.getPersonById(userId);

        return parent.map(p -> {
            var relation = relationshipService.getChildren(userId)
                    .stream().map(PersonsConverter::convertToPersonDto)
                    .collect(Collectors.toList());

            var parentRelation = new ParentRelation()
                    .parent(PersonsConverter.convertToPersonDto(p))
                    .children(relation);

            return ResponseEntity.ok(parentRelation);
        }).orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<RelativeRelationDTO> getRelationBetween(final Long userId1, final Long userId2) {
        var rel = relationshipService.getRelationBetween(userId1, userId2);

        var relation = new RelativeRelationDTO()
                .person1(PersonsConverter.convertToPersonDto(rel.getPerson1()))
                .person2(PersonsConverter.convertToPersonDto(rel.getPerson2()))
                .relation(rel.getRelation());

        return ResponseEntity.ok(relation);
    }

    @Override
    public ResponseEntity<Address> getAddressByUserId(final Long userId) {
        var address = addressService.getAddressByUserId(userId)
                .map(AddressConverter::convertToAddressDto);

        return address
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Phone> getPhoneByUserId(final Long userId) {
        var phone = phoneService.getPhoneByUserId(userId)
                .map(PhoneConverter::convertToPhoneDto);

        return phone
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
