/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.token;
import java.util.function.Function;

/**
 *
 * @author leijurv
 * @param <T>
 */
final class TokenCustom<T> implements Token<T> {
    final TokenType tokenType;
    final T data;
    final Function<T, String> toStr;
    TokenCustom(TokenType tokenType, Object data, Class<T> cl, Function<T, String> toString) {
        this.tokenType = tokenType;
        this.data = cl == null ? null : cl.cast(data);
        this.toStr = toString;
    }
    @Override
    public String toString() {
        return toStr.apply(data);
    }
    @Override
    public boolean equals(Object o) {
        if (o != null && o.getClass() != getClass()) {
            return false;
        }
        return toString().equals(o + "");
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public TokenType tokenType() {
        return tokenType;
    }
    @Override
    public T data() {
        return data;
    }

//public String findConflicts() {
//        Stream<Tuple<Room, List<String>>> roomConf
//                = Room.getRooms().parallelStream().map(room->new Tuple<Room, List<String>>(room,
//                                Stream.of(Block.blocks).parallel().map(
//                                        block->new Tuple<Block, List<Section>>(
//                                                block,
//                                                sections.parallelStream().filter(section->room.equals(locations.get(section))
//                                                        && block.equals(timings.get(section))).collect(Collectors.toList())
//                                        )
//                                ).filter(tuple->tuple.b.size() > 1).
//                                map(tuple->"\"In room " + room + " during " + tuple.a + ", there are classes " + tuple.b + "\"").
//                                collect(Collectors.toList()))
//                ).filter(tuple->!tuple.b.isEmpty());
//        Stream<Tuple<Teacher, List<String>>> teacherConf = teacherList.parallelStream().map(teacher->new Tuple<Teacher, List<String>>(teacher,
//                Stream.of(Block.blocks).parallel().map(
//                        block->new Tuple<Block, List<Section>>(
//                                block,
//                                sections.parallelStream().filter(section->teacher.equals(teachers.get(section)) && block.equals(timings.get(section))).collect(Collectors.toList())
//                        )
//                ).filter(tuple->tuple.b.size() > 1).map(tuple->"\"Teacher " + teacher + " during " + tuple.a + " is teaching classes " + tuple.b + "\"").collect(Collectors.toList()))
//        ).filter(tuple->!tuple.b.isEmpty());
//        String roomConflicts = roomConf.map(tuple->"\n\"" + tuple.a + "\":" + tuple.b).collect(Collectors.joining(",", "{", "}"));
//        String teacherConflicts = teacherConf.map(tuple->"\n\"" + tuple.a + "\":" + tuple.b).collect(Collectors.joining(",", "{", "}"));
//        Stream<Tuple<Student, List<String>>> studentConf = students.stream().map(student->{
//            Map<Block, List<Section>> map = roster.getSections(student).stream().collect(Collectors.groupingBy(section->timings.get(section)));
//            List<String> conf = Stream.of(Block.blocks).map(block->new Tuple<Block, List<Section>>(block, map.get(block))).filter(tuple->tuple.b != null && tuple.b.size() > 1).map(tuple->"\"Student " + student + " during " + tuple.a + " is in classes " + tuple.b + "\"").collect(Collectors.toList());
//            return new Tuple<Student, List<String>>(student, conf);
//        }).filter(tuple->!tuple.b.isEmpty());
//        String studentConflicts = studentConf.map(tuple->"\n\"" + tuple.a + "\":" + tuple.b).collect(Collectors.joining(",", "{", "}"));
//        return "{room:" + roomConflicts + ",\nteacher:" + teacherConflicts + ",\nstudent:" + studentConflicts + "}";
//    }
}
