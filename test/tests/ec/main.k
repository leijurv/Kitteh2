import ../bigint/main b
import base

main(){
	x:=(b::bigint*)base::basepointX()
	b::bigint* y=(b::bigint*)base::basepointY()
	modulus:=(b::bigint*)base::modulus()
	b::output(x)
	b::output(y)
	b::output(modulus)
}