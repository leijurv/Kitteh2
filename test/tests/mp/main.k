import ../testRSA r
main(){
    dividemod(fromLong(19),fromLong(10))
    long a=293465
    long b=836794
    long c=875623
    print(r::mp(a,b,c))
    mp:=modpow( fromLong(a),fromLong(b),fromLong(c) )
    output(mp)
    //verification: http://www.wolframalpha.com/input/?i=293465%5E836794+mod+875623
    //lol
    bigint* m=fromLong(1)
    leftshift(m,195)
    output(m)
    output(modpow(fromLong(a),fromLong(b),m))
    //http://www.wolframalpha.com/input/?i=293465%5E836794+mod+2%5E195
}