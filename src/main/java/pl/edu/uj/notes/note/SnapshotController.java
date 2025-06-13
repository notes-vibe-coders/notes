package pl.edu.uj.notes.note;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notes/{noteId}/snapshot")
@RequiredArgsConstructor
class SnapshotController {

  private final SnapshotService snapshotService;

  @GetMapping
  List<SnapshotDTO> snapshots(@PathVariable("noteId") String noteId) {
    return snapshotService.getSnapshotsByNoteId(noteId);
  }

  @PatchMapping("/{snapshotId}")
  SnapshotDTO restoreSnapshot(
      @PathVariable("noteId") String noteId, @PathVariable("snapshotId") String snapshotId) {
    return snapshotService.restoreSnapshot(noteId, snapshotId);
  }
}
