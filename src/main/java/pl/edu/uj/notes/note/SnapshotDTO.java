package pl.edu.uj.notes.note;

record SnapshotDTO(String id, String noteId, String content, long createdAt) {

  static SnapshotDTO from(NoteSnapshot snapshot) {
    return new SnapshotDTO(
        snapshot.getId(),
        snapshot.getNoteId().getId(),
        snapshot.getContent(),
        snapshot.getCreatedAt().toEpochMilli());
  }
}
