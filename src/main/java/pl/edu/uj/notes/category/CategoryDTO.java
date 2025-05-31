package pl.edu.uj.notes.category;

import java.util.List;
import pl.edu.uj.notes.note.NoteDTO;

record CategoryDTO(String id, String name, List<NoteDTO> notes) {}
