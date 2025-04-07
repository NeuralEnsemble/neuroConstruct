import neuroml
from neuroml import Segment, SegmentGroup, Morphology, Cell
import neuroml.writers as writers

def parse_swc(file_path):
    with open(file_path, 'r') as file:
        lines = file.readlines()

    segments = []
    for line in lines:
        if not line.startswith('#'):
            parts = line.strip().split()
            if len(parts) == 7:
                id, type, x, y, z, radius, parent = map(float, parts)
                segment = Segment(
                    id=int(id),
                    parent=int(parent) if int(parent) != -1 else None,
                    proximal=None,
                    distal=None,
                    name=f"type_{int(type)}",
                    group=f"custom_{int(type)}" if int(type) > 3 else None
                )
                segments.append(segment)

    return segments

def create_neuroml(segments, output_path):
    cell = Cell(id="swc_cell")
    morphology = Morphology(id="morph")
    cell.morphology = morphology

    for segment in segments:
        morphology.segments.append(segment)

    doc = neuroml.NeuroMLDocument(id="swc_to_nml")
    doc.cells.append(cell)

    writers.NeuroMLWriter.write(doc, output_path)
    print(f"NeuroML file written to {output_path}")

if __name__ == "__main__":
    swc_file = "path/to/your/input.swc"
    nml_file = "path/to/your/output.nml"
    segments = parse_swc(swc_file)
    create_neuroml(segments, nml_file)

