import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class SlidePuzzle {

	static int questListStart = 0;
	static int questListEnd = 5000;
	static int moveBytesLimit = 80;
	static int secTimeLimit = 60;
	final static int limitBaseUp = 2;
	final static long backMapLimit = 1200000;
	final static int backMapLimitMagnif = 4;
	final static byte[] slideCharArray = {'1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0'};
	final static byte[] directionBytesArray = {'L','R','U','D'};

	static long startTime = -1;
	static int limitCounter = -1;
	static int backLimitCounter = -1;
	static int width = -1;
	static int height = -1;
	static int[] directionIntArray = null;
	static byte[] globalQuestBytes = null;
	static byte[] globalMoveBytes = null;
	static int globalZeroPos = -1;
	static int globalPreDirectionInt = -1;
	static byte globalPreTargetByte = -1;
	static byte[] answerQuestBytes = null;
	static byte[] answerMoveBytes = null;
	static HashMap<String, byte[]> backMap = null;
	static HashMap<String, byte[]> moveMap = null;
	static int clearCount = 0;
	static String[] questStringArray = null;
	static String[] moveResultStringArray = null;
	static int restBoradCount = -1;

	public static void main(String[] args) {
		try {
			if(args.length>0 && args[0]!=null) questListStart = Integer.parseInt(args[0]);
			if(args.length>1 && args[1]!=null) questListEnd = Integer.parseInt(args[1]);
			if(args.length>2 && args[2]!=null) moveBytesLimit = Integer.parseInt(args[2]);
			if(args.length>3 && args[3]!=null) secTimeLimit = Integer.parseInt(args[3]);
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}

		int[] restTurnArray = null;
		int restBoradCount = -1;
		String[] questStringArray = null;

		FileReader fileReader = null;
		BufferedReader reader = null;
		try {
			fileReader = new FileReader("./src/question.txt");
			reader = new BufferedReader(fileReader);
			int n=0;
			while (true){
				String line = reader.readLine();
				if (line==null){
					break;
				}
				if(n==0) {
					String[] restTurnStringArray = line.split(" ");
					int restTurnLength = restTurnStringArray.length;
					restTurnArray = new int[restTurnLength];
					for(int i=0; i<restTurnLength; i++) {
						restTurnArray[i] = Integer.parseInt(restTurnStringArray[i]);
					}
				} else if(n==1) {
					restBoradCount = Integer.parseInt(line);
					questStringArray = new String[restBoradCount];
				} else if(n>1) {
					questStringArray[n-2] = line;
				}
				n++;
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if(fileReader!=null) fileReader.close();
			} catch(Exception e) {}
			try {
				if(reader!=null) reader.close();
			} catch(Exception e) {}
		}
		moveResultStringArray = new String[restBoradCount];
		try {
			fileReader = new FileReader("./src/answer.txt");
			reader = new BufferedReader(fileReader);
			int n=0;
			while (true){
				String line = reader.readLine();
				if (line==null){
					break;
				}
				moveResultStringArray[n] = line;
				n++;
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if(fileReader!=null) fileReader.close();
			} catch(Exception e) {}
			try {
				if(reader!=null) reader.close();
			} catch(Exception e) {}
		}

	//	answerWriteRead();
	//	if(questStringArray==null) return;

		for(int k=0; k<questStringArray.length; k++) {
			moveMap = new HashMap<String, byte[]>();
			backMap = new HashMap<String, byte[]>();
			String questString = questStringArray[k];
		//	String questString = "3,3,168452=30";
		//	String questString = "4,4,32465871FAC0=9BE";
		//	String questString = "6,6,1A3B458J26EZKF09LUD7GCIOP==MNTVWXYSH";
			String[] questParamArray = questString.split(",");
			width = Integer.parseInt(questParamArray[0]);
			height = Integer.parseInt(questParamArray[1]);
			byte[] thisQuestBytes = questParamArray[2].getBytes();
		//	System.out.println(Arrays.toString(thisQuestBytes));
			answerQuestBytes = getAnswerArray(thisQuestBytes);
		//	System.out.println(new String(answerQuestBytes));

			if(k<questListStart || questListEnd<=k) continue;
			if(moveResultStringArray[k]!=null && moveResultStringArray[k].length()>0) continue;

			answerMoveBytes = null;
			limitCounter = checkMaxStep(thisQuestBytes);
			directionIntArray = new int[directionBytesArray.length];
			directionIntArray[0] = -1;
			directionIntArray[1] = 1;
			directionIntArray[2] = width*-1;
			directionIntArray[3] = width;
			long questStartTime = Calendar.getInstance().getTimeInMillis();
			backLimitCounter = (limitCounter/backMapLimitMagnif);
			System.out.println("k:"+k+" thisQuestBytes:"+new String(thisQuestBytes)+" width:"+width+" height:"+height+" moveBytesLimit:"+moveBytesLimit+" limitCounter:"+limitCounter+" secTimeLimit:"+secTimeLimit);
			try {
				if(limitCounter<=moveBytesLimit) {
					globalQuestBytes = answerQuestBytes;
					globalZeroPos = getZeroPosition(globalQuestBytes);
					globalPreDirectionInt = 0;
					globalMoveBytes = new byte[0];
					answerQuestBytes = thisQuestBytes;
				//	System.out.println(new String(globalQuestBytes)+" "+new String(answerQuestBytes)+" "+new String(thisQuestBytes));
					try {
						backSearch();
					} catch(Error e) {
						throw e;
					}
					System.out.println("k:"+k+" thisQuestBytes:"+new String(thisQuestBytes)+" width:"+width+" height:"+height+" moveBytesLimit:"+moveBytesLimit+" limitCounter:"+limitCounter+" secTimeLimit:"+secTimeLimit+" backMap.size():"+backMap.size());
					globalQuestBytes = thisQuestBytes;
					globalZeroPos = getZeroPosition(globalQuestBytes);
					globalPreDirectionInt = 0;
					globalMoveBytes = new byte[0];
					answerQuestBytes = getAnswerArray(thisQuestBytes);
					for(; limitCounter<=moveBytesLimit; limitCounter+=limitBaseUp) {
					//	System.out.println("k:"+k+" thisQuestBytes:"+new String(thisQuestBytes)+" width:"+width+" height:"+height+" moveBytesLimit:"+moveBytesLimit+" limitCounter:"+limitCounter+" secTimeLimit:"+secTimeLimit);
						startTime = Calendar.getInstance().getTimeInMillis();
						try {
							search();
						} catch(Error e) {
							throw e;
						}
						if(answerMoveBytes!=null) break;
					}
				}
			} catch(Error e) {
				moveMap = null;
				backMap = null;
				System.gc();
				e.printStackTrace();
			}
			if(answerMoveBytes!=null) {
				System.out.println("--- k:"+k+" answerMoveBytes:"+new String(answerMoveBytes)+" questEndTime:"+(Calendar.getInstance().getTimeInMillis()-questStartTime)/1000);
				moveResultStringArray[k] = new String(answerMoveBytes);
				clearCount++;
			} else {
				System.out.println("k:"+k+" questEndTime:"+(Calendar.getInstance().getTimeInMillis()-questStartTime)/1000);
			}
		//	answerWriteRead();
			try {
				fileReader = new FileReader("./src/answer.txt");
				reader = new BufferedReader(fileReader);
				int n=0;
				while (true){
					String line = reader.readLine();
					if (line==null){
						break;
					}
					if(moveResultStringArray[n]==null || moveResultStringArray[n].length()<=0) {
						moveResultStringArray[n] = line;
					}
					n++;
				}
			} catch(Exception e) {
				System.out.println(e.getMessage());
			} finally {
				try {
					if(fileReader!=null) fileReader.close();
				} catch(Exception e) {}
				try {
					if(reader!=null) reader.close();
				} catch(Exception e) {}
			}
			if(answerMoveBytes!=null) {
				FileWriter fileWriter = null;
				BufferedWriter writer = null;
				try {
					fileWriter = new FileWriter("./src/answer.txt");
					writer = new BufferedWriter(fileWriter);
					for(int i=0; i<moveResultStringArray.length; i++) {
						String lineBuf = moveResultStringArray[i];
						if(lineBuf==null) lineBuf = "";
						writer.write(lineBuf+"\n");
					//	System.out.println("i:"+i+" lineBuf:"+lineBuf);
					}
					writer.flush();
				} catch(Exception e) {
					System.out.println(e.getMessage());
				} finally {
					try {
						if(fileWriter!=null) fileWriter.close();
					} catch(Exception e) {}
					try {
						if(writer!=null) writer.close();
					} catch(Exception e) {}
				}
				try {
					fileReader = new FileReader("./src/answer.txt");
					reader = new BufferedReader(fileReader);
					int n=0;
					while (true){
						String line = reader.readLine();
						if (line==null){
							break;
						}
						moveResultStringArray[n] = line;
						n++;
					}
				} catch(Exception e) {
					System.out.println(e.getMessage());
				} finally {
					try {
						if(fileReader!=null) fileReader.close();
					} catch(Exception e) {}
					try {
						if(reader!=null) reader.close();
					} catch(Exception e) {}
				}
			}
		}
/*
		FileWriter fileWriter = null;
		BufferedWriter writer = null;
		try {
			fileWriter = new FileWriter("./src/answer.txt");
			writer = new BufferedWriter(fileWriter);
			for(int i=0; i<moveResultStringArray.length; i++) {
				String lineBuf = moveResultStringArray[i];
				if(lineBuf==null) lineBuf = "";
				writer.write(lineBuf+"\n");
			//	System.out.println("i:"+i+" lineBuf:"+lineBuf);
			}
			writer.flush();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if(fileWriter!=null) fileWriter.close();
			} catch(Exception e) {}
			try {
				if(writer!=null) writer.close();
			} catch(Exception e) {}
		}
*/
		System.out.println("[end] clearCount:"+clearCount);
	}

	static private void answerWriteRead() {
		int[] restTurnArray = null;
		FileReader fileReader = null;
		BufferedReader reader = null;

		if(moveResultStringArray!=null) {
			try {
				fileReader = new FileReader("./src/answer.txt");
				reader = new BufferedReader(fileReader);
				int n=0;
				while (true){
					String line = reader.readLine();
					if (line==null){
						break;
					}
					if(line.length()>0 && line.length()<moveResultStringArray[n].length())
					moveResultStringArray[n] = line;
					n++;
				}
			} catch(Exception e) {
				System.out.println(e.getMessage());
			} finally {
				try {
					if(fileReader!=null) fileReader.close();
				} catch(Exception e) {}
				try {
					if(reader!=null) reader.close();
				} catch(Exception e) {}
			}

			FileWriter fileWriter = null;
			BufferedWriter writer = null;
			try {
				fileWriter = new FileWriter("./src/answer.txt");
				writer = new BufferedWriter(fileWriter);
				for(int i=0; i<moveResultStringArray.length; i++) {
					String lineBuf = moveResultStringArray[i];
					if(lineBuf==null) lineBuf = "";
					writer.write(lineBuf+"\n");
				//	System.out.println("i:"+i+" lineBuf:"+lineBuf);
				}
				writer.flush();
			} catch(Exception e) {
				System.out.println(e.getMessage());
			} finally {
				try {
					if(fileWriter!=null) fileWriter.close();
				} catch(Exception e) {}
				try {
					if(writer!=null) writer.close();
				} catch(Exception e) {}
			}
		}

		if(questStringArray==null) {
			try {
				fileReader = new FileReader("./src/question.txt");
				reader = new BufferedReader(fileReader);
				int n=0;
				while (true){
					String line = reader.readLine();
					if (line==null){
						break;
					}
					if(n==0) {
						String[] restTurnStringArray = line.split(" ");
						int restTurnLength = restTurnStringArray.length;
						restTurnArray = new int[restTurnLength];
						for(int i=0; i<restTurnLength; i++) {
							restTurnArray[i] = Integer.parseInt(restTurnStringArray[i]);
						}
					} else if(n==1) {
						restBoradCount = Integer.parseInt(line);
						questStringArray = new String[restBoradCount];
					} else if(n>1) {
						questStringArray[n-2] = line;
					}
					n++;
				}
			} catch(Exception e) {
				System.out.println(e.getMessage());
			} finally {
				try {
					if(fileReader!=null) fileReader.close();
				} catch(Exception e) {}
				try {
					if(reader!=null) reader.close();
				} catch(Exception e) {}
			}
		}
		if(moveResultStringArray==null) moveResultStringArray = new String[restBoradCount];
		try {
			fileReader = new FileReader("./src/answer.txt");
			reader = new BufferedReader(fileReader);
			int n=0;
			while (true){
				String line = reader.readLine();
				if (line==null){
					break;
				}
				moveResultStringArray[n] = line;
				n++;
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if(fileReader!=null) fileReader.close();
			} catch(Exception e) {}
			try {
				if(reader!=null) reader.close();
			} catch(Exception e) {}
		}
	}

	static private void backSearch() {
		for(int i=0; i<directionBytesArray.length; i++) {
			int directionInt = directionIntArray[i];
			if(globalPreDirectionInt==(directionInt*-1)) continue;
			int targetPos = globalZeroPos+directionInt;
			if(targetPos<0 || answerQuestBytes.length<=targetPos) continue;
			if((directionInt%width)!=0 && (globalZeroPos/width)!=(targetPos)/width) continue;
			byte target = globalQuestBytes[targetPos];
			if(target=='=') continue;
			globalQuestBytes[globalZeroPos] = target;
			globalQuestBytes[targetPos] = '0';
			try {
				globalZeroPos += directionInt;
				try {
					byte[] moveBytes = new byte[globalMoveBytes.length+1];
					if(globalMoveBytes.length==0) {
						moveBytes[0] = directionBytesArray[i];
					} else {
						moveBytes = Arrays.copyOf(globalMoveBytes, moveBytes.length);
						moveBytes[moveBytes.length-1] = directionBytesArray[i];
					}
				//	if(backLimitCounter<checkMaxStep(globalQuestBytes)+moveBytes.length) {
				//		//	System.out.println("limitCounter:"+limitCounter);
				//			continue;
				//	}
					byte[] preMoveBytes = null;
					if((preMoveBytes = backMap.get(new String(globalQuestBytes)))!=null) {
					//	moveBytes = moveBytesRepeatFix(moveBytes);
					//	System.out.println("containsKey globalQuestBytes:"+new String(globalQuestBytes)+" moveBytes:"+new String(moveBytes)+" preMoveBytes:"+new String(preMoveBytes));
						if(preMoveBytes.length<=moveBytes.length) continue;
					}
					if(Arrays.equals(answerQuestBytes, globalQuestBytes)==true) {
					//	moveBytes = moveBytesRepeatFix(moveBytes);
						System.out.println("- backMap clear questBytes:"+new String(globalQuestBytes)+" moveBytes:"+new String(moveBytes));
						backLimitCounter = moveBytes.length;
						backMap.put(new String(globalQuestBytes), moveBytes);
						continue;
					}
					if(moveBytes.length>backLimitCounter) continue;
					if(backMap.size()>backMapLimit) continue;
					backMap.put(new String(globalQuestBytes), moveBytes);
				//	System.out.println("backMap.size():"+backMap.size()+" backMap globalQuestBytes:"+new String(globalQuestBytes)+" moveBytes:"+new String(moveBytes));
					globalPreDirectionInt = directionInt;
					globalMoveBytes = moveBytes;
					try {
						backSearch();
					} finally {
						globalPreDirectionInt = (directionInt*-1);
						globalMoveBytes = Arrays.copyOfRange(moveBytes, 0, moveBytes.length-1);
					}
				} finally {
					globalZeroPos += (directionInt*-1);
				}
			} finally {
				globalQuestBytes[globalZeroPos] = '0';
				globalQuestBytes[targetPos] = target;
			}
		}
	}

	static private int getZeroPosition(byte[] quest) {
		int zeroPos = -1;
		for(int i=0; i<quest.length; i++) {
			if(quest[i]=='0') {
				zeroPos = i;
				break;
			}
		}
		return zeroPos;
	}

	static private void search() {
		for(int i=0; i<directionBytesArray.length; i++) {
			int directionInt = directionIntArray[i];
			if(globalPreDirectionInt==(directionInt*-1)) continue;
			int targetPos = globalZeroPos+directionInt;
			if(targetPos<0 || answerQuestBytes.length<=targetPos) continue;
			if((directionInt%width)!=0 && (globalZeroPos/width)!=(targetPos)/width) continue;
			byte target = globalQuestBytes[targetPos];
			if(target=='=') continue;
			try {
				globalQuestBytes[globalZeroPos] = target;
				globalQuestBytes[targetPos] = '0';
				try {
					globalZeroPos += directionInt;
				//	System.out.println("quest:"+new String(quest)+" questBytes:"+new String(questBytes));
					byte[] moveBytes = new byte[globalMoveBytes.length+1];
					if(globalMoveBytes.length==0) {
						moveBytes[0] = directionBytesArray[i];
					} else {
						moveBytes = Arrays.copyOf(globalMoveBytes, moveBytes.length);
						moveBytes[moveBytes.length-1] = directionBytesArray[i];
					}
					if(limitCounter<checkMaxStep(globalQuestBytes)+moveBytes.length) {
					//	System.out.println("limitCounter:"+limitCounter);
						continue;
					}
					byte[] preMoveBytes = null;
					if((preMoveBytes = moveMap.get(new String(globalQuestBytes)))!=null) {
					//	moveBytes = moveBytesRepeatFix(moveBytes);
					//	System.out.println("moveMap.size():"+moveMap.size());
					//	System.out.println("containsKey globalQuestBytes:"+new String(globalQuestBytes)+" moveBytes:"+new String(moveBytes)+" preMoveBytes:"+new String(preMoveBytes));
						if(preMoveBytes.length<=moveBytes.length) continue;
					}
					byte[] backMoveBytes = null;
					if(Arrays.equals(answerQuestBytes, globalQuestBytes)==true) {
					//	moveBytes = moveBytesRepeatFix(moveBytes);
						//answerMoveBytes = Arrays.copyOf(moveBytes, moveBytes.length);
						answerMoveBytes = moveBytes;
						System.out.println("- clear questBytes:"+new String(globalQuestBytes)+" answerMoveBytes:"+new String(answerMoveBytes));
						limitCounter = answerMoveBytes.length;
						moveMap.put(new String(globalQuestBytes), moveBytes);
						continue;
					} else if((backMoveBytes = backMap.get(new String(globalQuestBytes)))!=null) {
						backMoveBytes = mirrorBytes(backMoveBytes);
						byte[] preAnswerMoveBytes = new byte[moveBytes.length+backMoveBytes.length];
						System.arraycopy(moveBytes, 0, preAnswerMoveBytes, 0, moveBytes.length);
						System.arraycopy(backMoveBytes, 0, preAnswerMoveBytes, moveBytes.length, backMoveBytes.length);
					//	preAnswerMoveBytes = moveBytesRepeatFix(preAnswerMoveBytes);
						if((preMoveBytes = moveMap.get(new String(answerQuestBytes)))!=null) {
						//	System.out.println("preMoveBytes:"+new String(preMoveBytes)+" preAnswerMoveBytes:"+new String(preAnswerMoveBytes));
							if(preMoveBytes.length<=preAnswerMoveBytes.length) continue;
						}
						//answerMoveBytes = Arrays.copyOf(preAnswerMoveBytes, preAnswerMoveBytes.length);
						answerMoveBytes = preAnswerMoveBytes;
						System.out.println("- backMap clear globalQuestBytes:"+new String(globalQuestBytes)+" answerMoveBytes:"+new String(answerMoveBytes)+" moveBytes:"+new String(moveBytes)+" backMoveBytes:"+new String(backMoveBytes));
						limitCounter = answerMoveBytes.length;
						moveMap.put(new String(answerQuestBytes), answerMoveBytes);
						continue;
					}
					if((Calendar.getInstance().getTimeInMillis()-startTime)/1000>secTimeLimit) continue;
					if(moveBytes.length>limitCounter) continue;
					moveMap.put(new String(globalQuestBytes), moveBytes);
				//	if((moveMap.size()%1000)==0) System.out.println("moveMap.size():"+moveMap.size()+" globalQuestBytes:"+new String(globalQuestBytes)+" moveBytes:"+new String(moveBytes));
					globalPreDirectionInt = directionInt;
					globalMoveBytes = moveBytes;
					try {
						search();
					} finally {
						moveMap.remove(new String(globalQuestBytes));
						globalPreDirectionInt = (directionInt*-1);
						globalMoveBytes = Arrays.copyOfRange(moveBytes, 0, moveBytes.length-1);
					}
				} finally {
					globalZeroPos += (directionInt*-1);
				}
			} finally {
				globalQuestBytes[globalZeroPos] = '0';
				globalQuestBytes[targetPos] = target;
			}
		}
	}

	static private int checkMaxStep(byte[] questBytes) {
		int status = 0;
		for(int i=0; i<questBytes.length; i++) {
			if(questBytes[i]=='0') continue;
			int questWidth = questBytes[i]%width;
			int questHeight = questBytes[i]/width;
			int answerWidth = answerQuestBytes[i]%width;
			int answerHeight = answerQuestBytes[i]/width;
			if(questWidth!=answerWidth) {
				if(questWidth>answerWidth) status += questWidth-answerWidth;
				if(questWidth<answerWidth) status += answerWidth-questWidth;
			}
			if(questHeight!=answerHeight) {
				if(questHeight>answerHeight) status += questHeight-answerHeight;
				if(questHeight<answerHeight) status += answerHeight-questHeight;
			}
		}
		return status;
	}

	static private byte[] moveBytesRepeatFix(byte[] moveBytes) {
		if(moveBytes==null || moveBytes.length<2) return moveBytes;
		byte[] newFixMoveBytes = moveBytesRepeatFix2(moveBytes);
		for(int j=0; j<newFixMoveBytes.length; j++) {
			byte[] bufFixBytes = moveBytesRepeatFix2(Arrays.copyOfRange(newFixMoveBytes, 0, newFixMoveBytes.length-j));
			byte[] bufTailBytes = Arrays.copyOfRange(newFixMoveBytes, newFixMoveBytes.length-j, newFixMoveBytes.length);
			newFixMoveBytes = new byte[bufFixBytes.length+bufTailBytes.length];
			System.arraycopy(bufFixBytes, 0, newFixMoveBytes, 0, bufFixBytes.length);
			if(bufTailBytes.length>0) System.arraycopy(bufTailBytes, 0, newFixMoveBytes, bufFixBytes.length, bufTailBytes.length);
		}
		return newFixMoveBytes;
	}

	static private byte[] moveBytesRepeatFix2(byte[] moveBytes) {
		if(moveBytes==null || moveBytes.length<2) return moveBytes;
	//	System.out.println("moveBytes:"+new String(moveBytes));
		byte[] orgMoveBytes = null;
		byte[] tgtMoveBytes = null;
		byte[] mirrorMoveBytes = null;
		byte[] newFixMoveBytes = Arrays.copyOf(moveBytes, moveBytes.length);
		int checkLength = (moveBytes.length/2)+1;
		for(int i=0; i<checkLength; i++) {
			orgMoveBytes = Arrays.copyOfRange(moveBytes, moveBytes.length-i, moveBytes.length);
		//	System.out.println(new String("org:"+new String(orgMoveBytes))+" moveBytes.length:"+moveBytes.length);
			tgtMoveBytes = Arrays.copyOfRange(moveBytes, moveBytes.length-(i*2), moveBytes.length-i);
		//	System.out.println(new String("tgt:"+new String(tgtMoveBytes))+" moveBytes.length:"+moveBytes.length);
			mirrorMoveBytes = mirrorBytes(tgtMoveBytes);
		//	System.out.println(new String("mirror:"+new String(mirrorMoveBytes))+" moveBytes.length:"+moveBytes.length);
			if(orgMoveBytes!=null && orgMoveBytes.length!=0) {
				if(Arrays.equals(orgMoveBytes, mirrorMoveBytes)==true) {
				//	System.out.println(new String("org:"+new String(orgMoveBytes))+" moveBytes.length:"+moveBytes.length);
				//	System.out.println(new String("mirror:"+new String(mirrorMoveBytes))+" moveBytes.length:"+moveBytes.length);
					newFixMoveBytes = moveBytesRepeatFix(Arrays.copyOfRange(moveBytes, 0, moveBytes.length-(i*2)));
				}
			}
		}
		return newFixMoveBytes;
	}

	static private byte[] mirrorBytes(byte[] inBytes) {
		if(inBytes==null) return null;
		byte[] outBytes = new byte[inBytes.length];
		for(int i=0; i<inBytes.length; i++) {
			for(int j=0; j<directionBytesArray.length; j++) {
				if(directionBytesArray[j]==inBytes[i]) {
					if(j%2==0) {
						outBytes[(inBytes.length-1)-i] = directionBytesArray[j+1];
					} else {
						outBytes[(inBytes.length-1)-i] = directionBytesArray[j-1];
					}
					break;
				}
			}
		}
		return outBytes;
	}

	static private byte[] getAnswerArray(byte[] quest) {
		byte[] answerArray = new byte[quest.length];
		for(int i=0; i<quest.length; i++) {
			if(quest[i]=='=') {
				answerArray[i] = '=';
			} else {
				answerArray[i] = slideCharArray[i];
				if(i==(quest.length-1)) answerArray[quest.length-1] = slideCharArray[slideCharArray.length-1];
			}
		}
		return answerArray;
	}
}
