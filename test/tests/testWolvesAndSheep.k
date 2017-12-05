main(){
	size:=5
	wolves:=newBoard(size)
	attacked:=newBoard(size)
	seed:=(long*)malloc(sizeof(long))
	*seed=1
	for true{
		for x:=0; x<size; x++{
			for y:=0; y<size; y++{
				wolves[x][y]=false
				attacked[x][y]=false
			}
		}
		for i:=0; i<size; i++{
			x:=r(seed,size)
			y:=r(seed,size)
			for wolves[x][y]{
				x=r(seed,size)
				y=r(seed,size)
			}
			wolves[x][y]=true
			for dirX:=0-1; dirX<=1; dirX++{
				for dirY:=0-1; dirY<=1; dirY++{
					if dirX==0 && dirY==0{
						continue
					}
					copyX:=x
					copyY:=y
					for copyX>=0 && copyY>=0 && copyX<size && copyY<size{
						attacked[copyX][copyY]=true
						copyX=copyX+dirX
						copyY=copyY+dirY
					}
				}
			}
		}
		open:=0
		for x:=0; x<size; x++{
			for y:=0; y<size; y++{
				if !attacked[x][y]{
					open++
				}
			}
		}
		if open>=3{
			for x:=0; x<size; x++{
				for y:=0; y<size; y++{
					if wolves[x][y]{
						writeByte('W')
						continue
					}
					if attacked[x][y]{
						writeByte('+')
						continue
					}
					writeByte('S')
				}
				writeByte(10)
			}
			print(*seed)
			break
		}
	}
}
func r(long* seed,int max)int{
	*seed=(*seed*5023+257)%2147483647
	res:= *seed%(long)max
	if res<(long)0{
		return 0-(int)res
	}
	return (int)res
}
newBoard(int size) bool**{
	board:=(bool**)malloc(size*sizeof(bool*))
	for i:=0; i<size; i++{
		board[i]=(bool*)malloc(size*sizeof(bool))
	}
	return board
}