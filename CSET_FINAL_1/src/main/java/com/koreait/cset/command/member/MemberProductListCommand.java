package com.koreait.cset.command.member;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.springframework.ui.Model;

import com.koreait.cset.common.CsetCommand;
import com.koreait.cset.common.PageMaker;
import com.koreait.cset.dao.MemberDAO;
import com.koreait.cset.dto.MemberDTO;
import com.koreait.cset.dto.ProductsDTO;

public class MemberProductListCommand implements CsetCommand {

	@Override
	public void execute(SqlSession sqlSession, Model model) {
		Map<String, Object> map = model.asMap();
		HttpServletResponse response = (HttpServletResponse)map.get("response");
		HttpServletRequest request = (HttpServletRequest)map.get("request");
		MemberDAO mDAO =sqlSession.getMapper(MemberDAO.class);
		ArrayList<ProductsDTO> plist = mDAO.selectProductList();

		
		HttpSession session = null;
		session=request.getSession();
		MemberDTO loginDTO = (MemberDTO)session.getAttribute("loginDTO");
		String mId =loginDTO.getmId();
		
		if(mId.equals("ADMIN")) {			
			model.addAttribute("plist",plist);
			
		}  else {
			PrintWriter out;
			try {
				response.setContentType("text/html; charset=utf-8");
				out = response.getWriter();												
				out.println("<script type='text/javascript'>");
				out.println("alert('관리자페이지입니다.');");
				out.println("history.back();");
				out.println("</script>");
				out.close();
				  
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		
		
	}

}
