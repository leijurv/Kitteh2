main(){ 

    fizz := (byte*)malloc(4)
    fizz[0] = 'F'
    fizz[1] = 'i'
    fizz[2] = 'z'
    fizz[3] = 'z'

    buzz:=(byte*)malloc(4)
    buzz[0] = 'B'
    buzz[1] = 'u'
    buzz[2] = 'z'
    buzz[3] = 'z'

    newline := (byte*)malloc(1)
    newline[0] = 10

    for i := 1; i < 1155; i++ { 
        if i % 15 == 0 {
            write(fizz, 4)
            write(buzz, 4)
            write(newline, 1)

        } else {
            if i % 3 == 0 {
                write(fizz, 4)
                write(newline, 1)
            } else {
                if i % 5 == 0 {
                    write(buzz, 4)    
                    write(newline, 1)
                } else {
                    print(i)
                }
            }
        }
    } 
}
