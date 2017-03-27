import util/array list
main(){
    test1()
    test2()
    test3()
    test4()
    test5()
    test6()
}
test1(){//2 5 6 7
    l:=list::newList()
    l.append((void*)5)
    l.append((void*)6)
    l.append((void*)7)
    l.push((void*)2)
    l.printAsLongs()
}
test2(){//2 5 6 7
    l:=list::newList()
    l.push((void*)5)
    l.append((void*)6)
    l.append((void*)7)
    l.push((void*)2)
    l.printAsLongs()
}
test3(){//5 6 7 2
    l:=list::newList()
    l.push((void*)5)
    l.append((void*)6)
    l.append((void*)7)
    l.append((void*)2)
    l.printAsLongs()
}
test4(){//7 5 6 2
    l:=list::newList()
    l.push((void*)5)
    l.append((void*)6)
    l.push((void*)7)
    l.append((void*)2)
    l.printAsLongs()
}
test5(){//2
    l:=list::newList()
    l.push((void*)2)
    l.printAsLongs()
}
test6(){//2
    l:=list::newList()
    l.append((void*)2)
    l.printAsLongs()
}