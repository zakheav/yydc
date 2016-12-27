package businessInfo;

public class time_increase {
	boolean leap_year( int year ){
		if( year%100 == 0 ){
			if( year%400 == 0 ){
				return true;
			} else{
				return false;
			}
		} else{
			if( year%4 == 0 ){
				return true;
			}
			return false;
		}
		
	}//判断闰年
	public long increase( long begin ){//按小时增加
		int year, month, day, hour;
		int cm=0, cd=0, ch=0;//进位标志
		String y, m, d, h;
		long tempTime = begin;
		hour = (int)(tempTime % 100); tempTime = tempTime / 100;
		day = (int)(tempTime % 100); tempTime = tempTime / 100;
		month = (int)(tempTime % 100); tempTime = tempTime / 100;
		year = (int)tempTime;
		long result = 0;
		
		hour = hour+1;
		if( hour == 24 ){
			hour = 0;
			ch = 1;
		}
		
		day = day + ch;
		if(day == 29){
			if( month == 2 && leap_year(year)==false ){//平年二月
				day = 1; cd = 1;
			}
		} else if( day == 30 ){
			if( month == 2 && leap_year(year)==true ){//闰年二月
				day = 1; cd = 1;
			}
		} else if( day == 31 ){
			if( month == 4 || month == 6 || month == 9 || month == 11 ){//小月
				day = 1; cd = 1;
			}
		} else if( day == 32 ){//大月
			day = 1; cd = 1;
		}
		
		month = month + cd;
		if( month == 13 ){
			month = 1; cm = 1;
		}
		
		year = year + cm;
		
		//System.out.println( year+" "+month+" "+day+" "+hour );
		h = hour<10 ? "0"+String.valueOf(hour) : String.valueOf(hour) ;  
		d = day<10 ? "0"+String.valueOf(day) : String.valueOf(day);
		m = month<10 ? "0"+String.valueOf(month) : String.valueOf(month);
		y = String.valueOf(year);
		result = Long.parseLong( y+m+d+h );
		return result;
	}

	public long increaseDay( long begin ){//按天增加
		int year, month, day, hour;
		int cm=0, cd=0;//进位标志
		String y, m, d, h;
		long tempTime = begin;
		hour = (int)(tempTime % 100); tempTime = tempTime / 100;
		day = (int)(tempTime % 100); tempTime = tempTime / 100;
		month = (int)(tempTime % 100); tempTime = tempTime / 100;
		year = (int)tempTime;
		long result = 0;
		
	
		day = day + 1;
		if(day == 29){
			if( month == 2 && leap_year(year)==false ){//平年二月
				day = 1; cd = 1;
			}
		} else if( day == 30 ){
			if( month == 2 && leap_year(year)==true ){//闰年二月
				day = 1; cd = 1;
			}
		} else if( day == 31 ){
			if( month == 4 || month == 6 || month == 9 || month == 11 ){//小月
				day = 1; cd = 1;
			}
		} else if( day == 32 ){//大月
			day = 1; cd = 1;
		}
		
		month = month + cd;
		if( month == 13 ){
			month = 1; cm = 1;
		}
		
		year = year + cm;
		
		//System.out.println( year+" "+month+" "+day+" "+hour );
		h = hour<10 ? "0"+String.valueOf(hour) : String.valueOf(hour) ;  
		d = day<10 ? "0"+String.valueOf(day) : String.valueOf(day);
		m = month<10 ? "0"+String.valueOf(month) : String.valueOf(month);
		y = String.valueOf(year);
		result = Long.parseLong( y+m+d+h );
		return result;
	}
}
