package pl.edu.uj.notes.note;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  ResponseEntity<Void> deleteNote(@Valid @RequestBody DeleteNoteRequest request) {
    noteService.deleteNote(request);
    return ResponseEntity.noContent().build();
  }
}
