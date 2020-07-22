# CSET_FINAL
2020 spring-framework





<h1>👔👖CLOTHES-SET👕👔<h1>스프링기반의류쇼핑몰


 <ul>
        <li>DB 연동을 활용한 회원가입, 로그인, 주문시스템, 게시	판, 리뷰 기능 구현 </li>
        <li>상품 구매금액에 따른 등급제와 포인트 적립 구현 </li>
        <li>관리자 페이지를 별도로 제작하여 회원 정보, 구매내역	을 별도로 관리할 수 있도록제작 </li>
    </ul>


------

<h2>Client</h2>

frontend technologies used in this project:

<ul>
    <li>JavaScript, HTML, CSS</li>
    <li>jQuery, Ajax</li>
    <li>SweetAlert, FontAwesome</li>    
</ul>


<h2>Server</h2>

backend technologies used in this project:

<ul>
    <li>Spring, Apache Tomcat</li>
    <li>Oracle DB, MyBatis, JDBC,</li>
    <li>JSON-simple</li>    
    <li>Javax(Mail)</li>    
</ul>


<h2>Open API </h2>

<ul>
	<li>DAUM 주소찾기</li>    
</ul>


---------------------

<h2>클래스 다이어그램</h2>

<img src="https://user-images.githubusercontent.com/59395774/88129300-b7011300-cc12-11ea-930c-bc1febccb7b4.png" width="80%">

# Member

------

로그인Command

```java
package com.koreait.cset.command.member;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.ibatis.session.SqlSession;
import org.springframework.ui.Model;
import com.koreait.cset.common.CsetCommand;
import com.koreait.cset.dao.MemberDAO;
import com.koreait.cset.dao.OrderDAO;
import com.koreait.cset.dto.MemberDTO;
//회원의 ID와 PW를 확인을하는 loginCommand
public class MemberLoginCommand implements CsetCommand {

	@Override
	public void execute(SqlSession sqlSession, Model model)  {
		Map<String, Object> map = model.asMap();
		HttpServletRequest request = (HttpServletRequest)map.get("request");
		HttpServletResponse response = (HttpServletResponse)map.get("response");
		
		String mId = request.getParameter("mId");
		String mPw = request.getParameter("mPw");
		
	  // MemberDTO   회원의 정보를 MemberDTO에담는다.
		MemberDTO mDTO = new MemberDTO();
		mDTO.setmId(mId);
		mDTO.setmPw(mPw);
		MemberDAO mDAO = sqlSession.getMapper(MemberDAO.class);
		OrderDAO oDAO = sqlSession.getMapper(OrderDAO.class);
		HttpSession session = null;
		
		MemberDTO loginDTO =mDAO.selectBymIdmPw(mDTO);
      //총주문금액
      //구매액에따를 회원등급을 부여하기위하여 로그인할때 정보를 가져오고 다음등급을 표시를 하기위해 로직을 구현
		int totalPrice = oDAO.orderSumMoney(mId);
		int gradeChangeLimit = 100000;
	
		if(loginDTO != null) { // 검색된 회원이 있으면
			char abc = loginDTO.getmGrade();
			String nowGrade =Character.toString(abc);				
			char nextGrade;
			if(totalPrice<100000) {
				nextGrade ='C';
				 gradeChangeLimit = 100000;
			}else if(totalPrice<400000) {
				nextGrade ='B';
				gradeChangeLimit = 400000;
			}else if(totalPrice<700000) {
				nextGrade ='A';
				gradeChangeLimit = 700000;
			}else {
				nextGrade ='A';
				gradeChangeLimit = 1000000;
			}
			
			if(nowGrade.equals("D")) {
				nextGrade ='C';
				 gradeChangeLimit = 100000;
			}else if(nowGrade.equals("C")) {
				nextGrade ='B';
				 gradeChangeLimit = 400000;
			}else if(nowGrade.equals("B")) {
				nextGrade ='A';
				 gradeChangeLimit = 700000;
			}else if(nowGrade.equals("A")) {
				nextGrade ='A';
				 gradeChangeLimit = 1000000;
			}
			
			
			// ** 세션에 회원정보 올리기 **
			session = request.getSession();
			session.setAttribute("loginDTO", loginDTO); // 로그인에 성공하면 회원정보가 session에 저장.
			session.setAttribute("totalPrice", totalPrice); //누적금액을 세션에 올림.
			session.setAttribute("nextGrade", nextGrade);//다음등급을 세션에 올림.
			session.setAttribute("gradeChangeLimit", gradeChangeLimit);//구매액제한등릅을 세션에올림.					
		}  else { //회원이일치하지않으면 alert을띄움
			PrintWriter out;
			try {
				response.setContentType("text/html; charset=utf-8");
				out = response.getWriter();
				out.println("<script type='text/javascript'>");
				out.println("alert('아이디와 비밀번호를확인해주세요');");
				out.println("history.back();");
				out.println("</script>");
				out.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
			
	}

}
```
<img src="https://user-images.githubusercontent.com/59395774/88129448-1c550400-cc13-11ea-915e-31412db5a669.png" width="80%">


-----

#### 

<ul>
    <li>회원의 마이페이지안에서는 회원의정보와 회원이 주문한내역 주문한 상품 정보가 필요</li>
    <li>회원테이블  주문테이블 상품테이블을 조인을 함.</li>
    <li>joinVO생성</li>
</ul>

<img src="https://user-images.githubusercontent.com/59395774/88129513-3db5f000-cc13-11ea-95dc-531c53f69e8f.png" width="80%">
<img src="https://user-images.githubusercontent.com/59395774/88129581-63db9000-cc13-11ea-99c5-3e4a5a9c09ef.png" width="80%">


-------

멤버 주요기능 1.
<img src="https://user-images.githubusercontent.com/59395774/88129623-7fdf3180-cc13-11ea-91ba-0a800177d945.png" width="80%">


​    

```xml
 //회원 상품주문 조회기간의 파라미터로 정보를 SELECT하는sql
<select id="memberOrderPeriod" resultType="com.koreait.cset.dto.JoinVO">
		SELECT M.MID , M.MNAME, P.PNAME,P.PPRICE , O.OAMOUNT, O.ODATE, O.ONO, P.PFILENAME ,P.PNO
		FROM ORDERS O , MEMBERS M , PRODUCTS P
		WHERE M.MNO = O.MNO
		AND P.PNO = O.PNO 
		AND M.MNO = #{param1}		
	<if test="param2 == null">
	  AND TO_DATE(TO_CHAR(O.ODATE, 'YYYY-MM-DD'), 'YYYY-MM-DD')
        	BETWEEN TO_DATE('2000-01-01', 'YYYY-MM-DD')  
      AND TO_DATE(#{param3}, 'YYYY-MM-DD')			
	</if>		
   <if test="param3 == null">
	AND TO_DATE(TO_CHAR(O.ODATE, 'YYYY-MM-DD'), 'YYYY-MM-DD') 
        BETWEEN TO_DATE(#{param2}, 'YYYY-MM-DD')  
   	AND TO_DATE(SYSDATE, 'YYYY-MM-DD')			
		</if>
		<if test="param2 != null and param3 != null">
			AND TO_DATE(TO_CHAR(O.ODATE, 'YYYY-MM-DD'), 'YYYY-MM-DD') 
             BETWEEN TO_DATE(#{param2}, 'YYYY-MM-DD') 
	        AND TO_DATE(#{param3}, 'YYYY-MM-DD')	
		</if>		
		ORDER BY TO_DATE(O.ODATE, 'YYYY-MM-DD') DESC
</select>

```

​    

-----

관리자

<img src="https://user-images.githubusercontent.com/59395774/88129689-b026d000-cc13-11ea-9ce5-82e23508636e.png" width="80%">


회원목록에서 체크박스를 선택한후의 회원삭제를 누르게되면  ajax를 통하여 데이터에스 회원제거

```javascript
$(document).ready(function(){
//1.관리자 회원삭제     
   $('#memberDeleteBtn').click(function(){
	   if( confirm('회원 삭제를 진행하시겠습니까?') ) {
	        var mIdArray = [];  //회원의 아이디를 배열로 담기
	        var mNoArray = []; //회원번호 배열로담기
	      
	         $('input[name="mId"]:checked').each(function(i){//체크된 리스트 저장
	            mIdArray.push($(this).val());         
	         });      
	         $('input[name="mId"]:checked ~ input[type="hidden"]').each(function(i){
                 //체크된 리스트 저장
	        	 mNoArray.push($('.mNo').val());         	          
	         });
	         var objParams = {
	                  "mIdList" : mIdArray  
	                  , "mNoList" : mNoArray
	         };
           
		      $.ajax({
		         url:'memberDeleteAdmin',
		         type:'post',
		         dataType: "json",
		         contentType :"application/x-www-form-urlencoded; charset=UTF-8",
		         data:objParams,
		         success : function(responseText) {
		            if(true){
		               swal({
		                   title: "회원삭제.",
		                   text: responseText.count+"명 ",
		                   icon: "success" 
		               });
		            }
		         },
		         error: function() {
		            swal({
		                title: "ajax통신실패.",
		                text: "",
		                icon: "warning" 
		            });
		         }
		      });//ajax
		   
	   } else {
		   alert('삭제를 취소합니다');
		   return;
	   } // if end
   });//btn
});
```

```java
	//24 클로셋회원 삭제하기(컨트롤러)
	@ResponseBody
	@RequestMapping(value="memberDeleteAdmin", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	public Object memberDeleteAdmin(@RequestParam(value="mIdList[]") List<String> mIdList
									, @RequestParam(value="mNoList[]") List<Integer> mNoList													
									, Model model) {
		MemberDAO mDAO = sqlSession.getMapper(MemberDAO.class);
		for(String mId:mIdList) {
			mDAO.memberDeleteReview(mId);
		}
		for(Integer mNo:mNoList) {
			mDAO.memberDeleteOrder(mNo);			
		}
		for(String mId:mIdList) {			
			mDAO.memberDeleteBoard(mId);
			mDAO.memberDeleteCart(mId);
			//회원을 제거하기위해 그동안회원의 기록을 제거를 해야합니다.
			mDAO.memeberDelete(mId);			
		}		
			int count = mIdList.size();	
			JSONObject obj = new JSONObject();
			obj.put("count",count );
		return obj.toJSONString();
	}
	
```

------

관리자가 회원의 등급과 포인트지급
<img src="https://user-images.githubusercontent.com/59395774/88129742-ccc30800-cc13-11ea-86e4-dd803f57d56a.png" width="80%">



```java
//ajax통신을하기위해서는 @ResponseBody 와 produces의 방식을 꼭 입력을 해야됨.
//20.관리자가 회원 등급변경
	@ResponseBody
	@RequestMapping(value="memberUpdateGrade", method=RequestMethod.POST, produces="text/html; charset=utf-8")
	public String memberUpdateGrade(@RequestParam("afterGrade") char afterGrade,
				     			  @RequestParam("mNo") int mNo,
								Model model) {
		
		MemberDAO mDAO = sqlSession.getMapper(MemberDAO.class);
		mDAO.memberUpdateGrade(afterGrade, mNo);		
		String responseText = afterGrade+"";
		return responseText;
	}
	
	//21.관리자가 회원 포인트 지급
	@ResponseBody
	@RequestMapping(value="memberUpdatePoint", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	public String memberUpdatePoint(@RequestParam("afterPoint") int afterPoint,
								 @RequestParam("mNo") int mNo,
								  Model model){
		JSONObject obj = new JSONObject();
		MemberDAO mDAO = sqlSession.getMapper(MemberDAO.class);
		mDAO.memberUpdatePoint(afterPoint, mNo);
		obj.put("result", afterPoint);
		return obj.toJSONString();
	}
	
```

```javascript

$(document).ready( function(){	 	
//1.회원등급변경		
	$('#updateGradeBtn').click(function(){
		$.ajax({
			url:'memberUpdateGrade',
			type:'POST',
			dataType:'text',
			data:'afterGrade='+ $( '#afterGrade option:selected' ).val() +'&mNo='+ $( '#mNo' ).val(), //select값 유의
			success : function(responseText) {
				var grade=  $('#afterPoint option:selected').val() ;
				 $('#grade').text(responseText);
					swal({
					    title: "등급이변경되었습니다.",
					    text: responseText+"변경",
					    icon: "success" 
					});				
			},
			error: function() {
				swal({
				    title: "ajax통신실패.",
				    text: "",
				    icon: "warning" 
				});
			}
		});//ajax
	});//btn	
//2.관리자가 회원 포인트 지급		
    function fn_calcPoint( point, afterPoint ){
           return point+afterPoint; // 값을 보기위한 단순함수 db에저장되는 데이터 보여주기용도
    }	
	$('#updatePointBtn').click(function(){
		$.ajax({
			url:'memberUpdatePoint',
			type:'POST',
			dataType:'json',
			data:'afterPoint='+ $( '#afterPoint option:selected' ).val() +'&mNo='+ $( '#mNo' ).val(),
			success : function(responseObject) {			
				var point = parseInt( $('#point').text() );
				var afterPoint = parseInt( $('#afterPoint option:selected').val() );

                 $('#point').text( fn_calcPoint( point, afterPoint ));
                 $('#point').css('color', 'blue').css('font-weight', 'bold');
				swal({
				    title: "포인트가 지급되었습니다",
				    text: responseObject.result+"포인트 지급!",
				    icon: "success" 
				});			
			},
			error: function() {
				swal({
				    title: "ajax통신실패.",
				    text: "",
				    icon: "warning" 
				});
			}
		});//ajax
	});//btn
 });//end
```
<img src="https://user-images.githubusercontent.com/59395774/88129776-e1070500-cc13-11ea-90da-bbe190d52df3.png" width="80%">


```jsp
<div class="admin-prod wrap">
<h1 class="page-title">상품 관리 페이지</h1>
<form>
<table>
   <caption class="blind">관리자 상품페이지 입니다.</caption>
   <thead>
   <tr>
   	<td colspan="9">
   		<input type="text" name="searchBox"/><input type="button" value="검색" onclick="fn_search(this.form)"/>
   	</td>
   </tr>
   <tr>
      <td>상품번호</td>
      <td>상품이름</td>
      <td>상품 이미지</td>
      <td>가격</td>
      <td>대분류</td>
      <td>소분류</td>
      <td>브랜드</td>
      <td>할인율(%)</td>
      <td>할인된가격</td>
   </tr>
   </thead>
   <tbody>
      <c:if test="${empty plist}">
            <tr> <td colspan="10">이미지글이 없습니다.</td></tr>
      </c:if>
      <c:if test="${not empty plist}">
            <c:forEach var="pDTO" items="${plist}" varStatus="k">
               <script type="text/javascript">
               
                  function fn_calcPrice( price, disrate ){
                     return price * ( 1 - (disrate/100) );
                  }               
                   $(document).ready( function(){
                      
                     var k = '${k.index}'; //forEach 로 반복되는 테이블안에서 하나의 레코드를 식별하기위함과 각 버튼과 데이터를 선택 위함.k변수선언                    
                     var price = parseInt( $('#price'+k).text() );
                     var disrate = parseInt( $('#disrate'+k).text() );
                           
                  //1.제품가격변경하기    
                     $('#updatePriceBtn'+k).click(function(){
                        $.ajax({
                           url:'memberUpdatePrice',
                           type:'POST',
                           dataType:'json',
                           data:'afterPrice='+ $( '#afterPrice'+k ).val() +'&pNo='+ $( '#pNo'+k ).val(),
                           success : function(responseObject) {
                              
                              $('#price'+k).text(responseObject.result);                                 
                              $('#price'+k).css('color', 'blue').css('font-weight', 'bold');
                              price = parseInt(responseObject.result);
                              $('#resultPrice'+k).text( Math.round( fn_calcPrice( price, disrate ) /100 )*100 );
                              $('#resultPrice'+k).css('color', 'blue').css('font-weight', 'bold');
                              swal({
                                  title: "가격이변경되었습니다.",
                                  text: "",
                                  icon: "success" });
                           },
                           error: function() {
                              swal({
                                  title: "가격을입력하세요.",
                                  text: "",
                                  icon: "warning"});
                           }
                        });//ajax
                     });//updatePriceBtn
                       
                  //2.제품할인률변경하기
                   $('#updateDisrateBtn'+k).click(function() {
                     $.ajax({
                        url:'memberUpdatedisrate',
                        type:'POST',
                        dataType:'json',
                        data:'afterDisrate='+ $( '#afterDisrate'+k ).val() +'&pNo='+ $( '#pNo'+k ).val(),
                        success : function(responseObject) {
                           if(true){                                 
                              $('#disrate'+k).text(responseObject.result+'%');
                              $('#disrate'+k).css('color', 'blue').css('font-weight', 'bold');
                              disrate = parseInt( responseObject.result );
                              $('#resultPrice'+k).text( Math.round( fn_calcPrice( price, disrate ) /100 )*100 );
                              $('#resultPrice'+k).css('color', 'blue').css('font-weight', 'bold');
                              swal({
                                  title: "할인율이 변경되었습니다..",
                                  text: "",
                                  icon: "success"
                              });
                           }else{                     
                           }
                        },   
                        error: function() {
                           swal({
                               title: "할인율을입력하세요.",
                               text: "",
                               icon: "warning"
                           });
                        }               
                     });//ajax
                  });//updateDisrateBtn
                     
                     
                     
                   });//end
                  
               </script>
                  <tr>                     
                     <td>
                        <span>${pDTO.pNo}</span>
                        <input type="hidden" id="pNo${k.index}" value="${pDTO.pNo}"/>
                     </td>
                     <td>${pDTO.pName}</td>                     
                     <td><img class="product-img small" alt="${pDTO.pFilename}" src="resources/product_photos/${pDTO.pFilename}"></td>
                     
                     <td class="price-change">
                        <span id="price${k.index }">${pDTO.pPrice}</span>
                        <span><input id="afterPrice${k.index }" name="afterPrice" type="text"></span>                        
                        <span><input id="updatePriceBtn${k.index }" type="button" value="가격변경"></span>
                     </td>                     
                     <td>${pDTO.pCategory}</td>
                     <td>${pDTO.pCategory_sub}</td>
                     <td>${pDTO.pBrand}</td>                     
                     <td class="price-change">
                        <span id="disrate${k.index }">${pDTO.pDisrate}%</span>
                        <span><input id="afterDisrate${k.index }" name="afterDisrate" type="text"></span>                        
                        <span><input id="updateDisrateBtn${k.index}" type="button" value="할인율변경"></span>
                     </td>               
                     <td>
                        <fmt:parseNumber var="test" integerOnly="true" value="${pDTO.pPrice*(1-(pDTO.pDisrate*0.01))}"/>
                        <span id="resultPrice${k.index }">
                          <%--  ${ (pDTO.pPrice*(1-(pDTO.pDisrate*0.01)) % 100) ge 50 ?  } --%>
                        </span>            
                     </td>                  
                  </tr>
            </c:forEach>
         </c:if>
   </tbody>

</table>
</form>
</div>
```

