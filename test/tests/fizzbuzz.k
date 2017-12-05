main(){ 
    for i := 1; i < 1155; i++ { 
        if i % 15 == 0 {
            print("FizzBuzz")
            writeByte(10)
        } else {
            if i % 3 == 0 {
                print("Fizz")
                writeByte(10)
            } else {
                if i % 5 == 0 {
                    print("Buzz") 
                    writeByte(10)
                } else {
                    print(i)
                }
            }
        }
    } 
}
