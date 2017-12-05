import ../testRSA r
import math/bigint
main(){
    dividemod(fromLong(19),fromLong(10))
    long a=293465
    long b=836794
    long c=875623
    print(r::mp(a,b,c))
    mp:=modpow( fromLong(a),fromLong(b),fromLong(c) )
    mp.output()
    //verification: http://www.wolframalpha.com/input/?i=293465%5E836794+mod+875623
    //lol
    bigint* m=fromLong(1)
    m.leftshift(195)
    m.output()
    modpow(fromLong(a),fromLong(b),m).output()
    //http://www.wolframalpha.com/input/?i=293465%5E836794+mod+2%5E195
}