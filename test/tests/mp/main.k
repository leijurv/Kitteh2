import ../bigint/main b
import ../testRSA r
main(){
        b::dividemod(b::fromLong(19),b::fromLong(10))
        long a=293465
        long b=836794
        long c=875623
         //a=33455
         //b=894
        c=845683
        print(r::mp(a,b,c))
      b::output(b::modpow( b::fromLong(a),b::fromLong(b),b::fromLong(c) ))
        //verification: http://www.wolframalpha.com/input/?i=293465%5E836794+mod+875623
        //lol
        m:=b::fromLong(1)
        b::leftshift(m,195)
        b::output(m)

b::output(b::modpow( b::fromLong(a),b::fromLong(b),m ))
        //http://www.wolframalpha.com/input/?i=293465%5E836794+mod+2%5E195


}

/*458238
458238
50216813883093446110686315385661331328818843555712276103168
26479579138900890545585007057654484294809155065274125137713
*/