main(){ 
	numX:=100
	xmin:=(float)(0-2)
	xmax:=(float)6/(float)10
	ymax:=(float)11/(float)10
	ymin:=(float)0-ymax
	numY:=(float)numX*((float)7/(float)12*(ymax-ymin)/(xmax-xmin))
	for y:=0; (float)y<numY; y++{
		for x:=0; x<100; x++{
			orb:=orbits(xmin+(xmax-xmin)*(float)x/(float)numX,ymin+(ymax-ymin)*(float)y/numY,100)
			if orb==100{
				writeByte('X')
				continue
			}
			if orb>10{
				writeByte('.')
				continue
			}
			if orb<3{
				writeByte(',')
				continue
			}
			writeByte(' ')
		}
		writeByte(10)
	}
}
orbits(float cx, float cy, int iterLimit)int{
	float zx=cx
	float zy=cy
	iter:=0
	for iter<iterLimit && zx*zx+zy*zy<(float)4{
		float nx=zx*zx-zy*zy+cx
		float ny=zx*zy+zx*zy+cy
		zx=nx
		zy=ny
		iter++
	}
	return iter
}