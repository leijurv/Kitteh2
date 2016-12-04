import ../bigint/main b
import ../testRSA r
main(){
	long a=293465
	long b=836794
	long c=875623
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