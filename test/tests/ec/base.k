import math/bigint
import ./main mayn
func basepointX()bigint*{
	return bigintFromHex("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798")
}
basepointY() bigint*{
	return bigintFromHex("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8")
}
basepoint() mayn::ecpoint*{
	bp:=(mayn::ecpoint*)malloc(sizeof(mayn::ecpoint))
	bp.x=basepointX()
	bp.y=basepointY()
	return bp
}
modulus() bigint*{
	twofivesix:=fromLong(1)
	twofivesix.leftshift(256)
	thirtytwo:=fromLong(1)
	thirtytwo.leftshift(32)
	wew:=twofivesix.subtract(thirtytwo)
	ninesevenseven:=fromLong(977)
	bigint* modulus=wew.subtract(ninesevenseven)
	(twofivesix).free()
	(thirtytwo).free()
	(wew).free()
	(ninesevenseven).free()
	return modulus
}