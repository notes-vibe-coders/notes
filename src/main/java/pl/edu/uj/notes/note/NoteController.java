package pl.edu.uj.notes.note;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/notes")
class NoteController {

  private final NoteService noteService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  ResponseEntity<Void> createNote(@Valid @RequestBody CreateNoteRequest request) throws Exception {
    var location = new URI("api/v1/notes/" + noteService.createNote(request));
    return ResponseEntity.created(location).build();
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  ResponseEntity<NoteDTO> getNote(@PathVariable @NotBlank String id) {
    return ResponseEntity.ok(noteService.getNote(id));
  }

  @GetMapping()
  @ResponseStatus(HttpStatus.OK)
  ResponseEntity<List<NoteDTO>> getAllNotes(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String content,
      @RequestParam(required = false) Boolean important) {
    return ResponseEntity.ok(noteService.getAllNotes(title, content, important));
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  ResponseEntity<Void> deleteNote(@Valid @RequestBody DeleteNoteRequest request) {
    noteService.deleteNote(request);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.ACCEPTED)
  ResponseEntity<NoteDTO> updateNote(
      @PathVariable @NotBlank String id, @Validated @RequestBody CreateNoteRequest request) {
    return ResponseEntity.accepted().body(noteService.updateNote(id, request));
  }

  @PatchMapping("/{id}/important")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  ResponseEntity<Void> markAsImportant(@PathVariable @NotBlank String id) {
    noteService.markAsImportant(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/archivized")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  ResponseEntity<Void> markAsArchivized(@PathVariable @NotBlank String id) {
    noteService.markAsArchivized(id);
    return ResponseEntity.noContent().build();
  }
}
