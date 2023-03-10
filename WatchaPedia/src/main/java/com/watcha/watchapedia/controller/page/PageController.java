package com.watcha.watchapedia.controller.page;

import com.watcha.watchapedia.model.dto.*;
import com.watcha.watchapedia.dto.response.UserResponse;
import com.watcha.watchapedia.model.entity.*;
import com.watcha.watchapedia.model.entity.type.FormStatus;
import com.watcha.watchapedia.model.network.Header;
import com.watcha.watchapedia.model.network.request.NoticeApiRequest;
import com.watcha.watchapedia.model.network.request.QnaRequest;
import com.watcha.watchapedia.model.network.response.*;
import com.watcha.watchapedia.model.repository.*;
import com.watcha.watchapedia.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class PageController {
    @Autowired
    public MovieApiLogicService movieApiLogicService;

    @Autowired
    public TvApiLogicService tvApiLogicService;

    @Autowired
    public BookApiLogicService bookApiLogicService;

    @Autowired
    public WebtoonApiLogicService webtoonApiLogicService;

    @Autowired
    public AdminApiLogicService adminApiLogicService;

    @Autowired
    public NoticeApiLogicService noticeApiLogicService;

    @Autowired
    public final GlobalMethodService globalMethodService;

    @Autowired
    public QnaService qnaService;

    @Autowired
    public CharacterApiLogicService characterApiLogicService;

    @Autowired
    public AdvertiseApiLogicService advertiseApiLogicService;


    //???????????? ?????? ?????? url??? ?????????????????? ?????? ???????????? ?????? ?????? (??????????????? ????????????)
    //* ???????????? ????????? : HttpServletRequest ??????
    public ModelAndView loginCheck(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if(session == null){
            System.out.println("????????? ????????????");
            return new ModelAndView("/0_login/Login");
        }else{
            System.out.println("????????? ????????????");
        }
        return null;
    }

    //????????? ??????, ??????????????? ????????? thymeleaf??? ???????????? ?????????
    //* ???????????? ????????? : HttpServletRequest ??????
    //* ???????????? ????????? : ModelAndView Templates ??????
    public ModelAndView loginInfo(HttpServletRequest request, String view){
        HttpSession session = request.getSession(false);
        Long adminIdx = null;
        String adminId = null;
        String adminName = null;
        String adminType = null;
        if(session != null){
            adminIdx = (Long)session.getAttribute("adminIdx");
            adminId = (String)session.getAttribute("adminId");
            adminName = (String)session.getAttribute("adminName");
            adminType = (String)session.getAttribute("adminType");
            System.out.println(adminIdx + adminId + adminName + adminType);
            return new ModelAndView(view)
                    .addObject("adminIdx",adminIdx)
                    .addObject("adminId",adminId)
                    .addObject("adminName",adminName)
                    .addObject("adminType",adminType);
        }else{
            return new ModelAndView(view);
        }
    }

    //map.addAttribute??? ???????????? ????????? ????????? ???????????? ?????? ?????????
    //* ???????????? ????????? : HttpServletRequest ??????
    //* ???????????? ????????? : ModelMap ??????
    public ModelMap loginModelInfo(HttpServletRequest request, ModelMap map){
        HttpSession session = request.getSession(false);
        if(session != null){
            map.addAttribute("adminIdx",(Long)session.getAttribute("adminIdx"));
            map.addAttribute("adminId",(String)session.getAttribute("adminId"));
            map.addAttribute("adminName",(String)session.getAttribute("adminName"));
            map.addAttribute("adminType",(String)session.getAttribute("adminType"));
            return map;
        }else{
            return null;
        }
    }

    //????????? ???????????? ??????
    @GetMapping(path="/login")
    public ModelAndView login(HttpServletRequest request){
        //?????? ???????????? ????????????, ????????? ???????????? ???????????? ???????????? ????????????
        HttpSession session = request.getSession(false);
        if(session != null){
            return loginInfo(request, "/index");
        }

        return new ModelAndView("/0_login/Login");
    }

    //???????????? ?????? (Servlet ??????)
    @PostMapping(path="/loginOk")
    public String loginOk(HttpServletRequest request, String adminId, String adminPw){
        //id, pw ???????????? ????????? ????????? Header?????? ??????
        Header<AdminApiResponse> accountCheck = adminApiLogicService.read(adminId, adminPw);

        //???????????? ????????? data??? ????????? ?????? ??????!
        if(accountCheck.getData() != null){
            HttpSession session = request.getSession();
            Long adminIdx = accountCheck.getData().getAdminIdx();
            String adminName = accountCheck.getData().getAdminName();
            String adminType = accountCheck.getData().getAdminType();
            session.setAttribute("adminIdx", adminIdx); //?????? ???????????? ????????? ???????????? ?????? ?????????Idx
            session.setAttribute("adminId", adminId);
            session.setAttribute("adminName", adminName);
            session.setAttribute("adminType", adminType);   //???????????????, ??????????????? ????????? ????????? ??????
            //????????? ???????????? ?????????????????? ??????
            return "redirect:/";
        }else{
            //????????? ?????? => ????????? ???????????? ?????? ??????
            return "redirect:/login";
        }
    }

    //???????????? ??????
    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        session.invalidate();
        return "redirect:/";
    }

    //????????????
    @GetMapping(path="")
    public ModelAndView index(HttpServletRequest request){
        //Login??? ????????????, ????????? ????????? HTML??? ?????????
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/index");
    }

    //????????????
    @GetMapping(path="/notice")
    public ModelAndView notice(HttpServletRequest request){
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        List<NoticeApiResponse> notice = userService.noticeAll();
        return loginInfo(request, "/1_notice/Notice").addObject("notices",noticeApiLogicService.noticeList()).addObject("notice",notice);
    }


    @GetMapping(path="/notice_edit/{ntcIdx}")
    public ModelAndView notice_edit(@PathVariable Long ntcIdx,HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        Header<NoticeApiResponse> api = noticeApiLogicService.read(ntcIdx);
        return loginInfo(request, "/1_notice/Notice_Edit").addObject("notice",api.getData());
    }

    @GetMapping(path="/notice_view/{ntcIdx}")
    public ModelAndView notice_view(@PathVariable Long ntcIdx, HttpServletRequest request){
        Header<NoticeApiResponse> api = noticeApiLogicService.read(ntcIdx);
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/1_notice/Notice_View").addObject("notice",api.getData());
    }



    @GetMapping(path="/notice_write")
    public ModelAndView notice_write(HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/1_notice/Notice_Write");
    }

    //noticeOk ??????!
//    @PostMapping(path = "/noticeOk")
//    public String noticeOk(MultipartFile file, String ntcTitle, String ntcText, String ntcBtnText, String ntcBtnColor){
//
//        String fileName = file.getOriginalFilename();
//        System.out.println("fileName : " + fileName);
//        String filePath = "C:\\image\\"+fileName;
//
//
//        try {
//            //?????? ??????
//            String folderPath = "C:\\image";
//            File folder = new File(folderPath);
//
//            if(!folder.exists()){
//                folder.mkdir();
//            }
//
//            FileOutputStream fos = new FileOutputStream(filePath);
//            InputStream is = file.getInputStream();
//            int readCount = 0;
//            byte[] buffer = new byte[1024];
//            while((readCount = is.read(buffer)) != -1){
//                fos.write(buffer, 0, readCount);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("Catch?????? ????????????...");
//
//        NoticeApiRequest noticeApiRequest = NoticeApiRequest.builder()
//                .ntcTitle(ntcTitle)
//                .ntcText(ntcText)
//                .ntcImagepath(filePath)
//                .ntcBtnColor(ntcBtnColor)
//                .ntcBtnText(ntcBtnText)
//                .build();
//
//        noticeApiLogicService.create(Header.OK(noticeApiRequest));
//
//
//
//
//
//        return "redirect:/";
//    }
    //noticeOk ???!
















    // qna ?????????
    @GetMapping(path="/qna")
    public String qna(ModelMap map, HttpServletRequest request){
        loginModelInfo(request,map);

        map.addAttribute("qnas", qnaService.searchQnas());
        return "/2_qna/QnA";
    }

    // qna ????????? ?????? ?????? ??????
    final QnaRepository qnaRepository;
    @GetMapping(path="/qna/{qnaIdx}")
    public String qnadetail(@PathVariable Long qnaIdx, ModelMap map, HttpServletRequest request){
        loginModelInfo(request,map);    //??????????????? ?????? map????????? ?????????
        Optional<Qna> qna = qnaRepository.findById(qnaIdx);
        QnaResponse qnaResponse = QnaResponse.from(QnaDto.from(qna.get()));
        map.addAttribute("qna", qnaResponse);
        return "/2_qna/QnA_Reply";
    }

    // qna ?????? ?????? ??????
    @GetMapping("/qnaview")
    public String QnaView(ModelMap map, HttpServletRequest request){
        loginModelInfo(request,map);
        map.addAttribute("view" , FormStatus.CREATE);
        return "/2_qna/QnA_View";
    }

    @PostMapping ("/qnaview")
    public String postqnaview(QnaRequest qnaRequest, ModelMap map, HttpServletRequest request) {
        loginModelInfo(request,map);
        qnaService.saveQna(qnaRequest.toDto());
        return "redirect:/qna";
    }

    // qna ?????? ?????? ????????? ?????????
    @GetMapping("/qna/{qnaIdx}/qnaview")
    public String updateQnaVieW(@PathVariable Long qnaIdx, ModelMap map, HttpServletRequest request){
        loginModelInfo(request,map);
        QnaResponse qna = QnaResponse.from(qnaService.getQna(qnaIdx));
        map.addAttribute("qna", qna);
        map.addAttribute("formStatus", FormStatus.UPDATE);
        return "/2_qna/QnA_View";
    }

    // qna ?????? ?????? ????????? ?????????





    /*   @RequestParam Spring MVC?????? ?????? ????????? ????????? ?????? ??????????????? ????????? ??? ??????,
    ????????? ????????? ????????? ????????? ????????? ??? ????????? @RequestParam(required = false)??? ????????????
    required ????????? ???????????? ?????? ????????? ?????????????????? ???????????? ????????? ????????? ???????????? ?????????
    String qnaText??? html ?????? name = qnaText??? ?????? ?????? ????????? qnaview??? ????????? ??? ??????
     */

    @GetMapping(path="/contents/book")
    public ModelAndView book(HttpServletRequest request){
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/3_contents/book/book").addObject("books",bookApiLogicService.bookList());
    }
    @GetMapping(path="/contents/book_edit/{bookIdx}")
    public ModelAndView bookEdit(@PathVariable Long bookIdx,HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        // ????????? Check ???!
        Header<BookApiResponse> api = bookApiLogicService.read(bookIdx);
        return loginInfo(request, "/3_contents/book/book_edit").addObject("book",api.getData());
//        return new ModelAndView("/3_contents/book/book_edit");
    }

    @GetMapping(path="/contents/book_write")
    public ModelAndView bookWrite(HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        // ????????? Check ???!
        return loginInfo(request, "/3_contents/book/book_write");
    }
    @GetMapping(path="/contents/book_detail/{bookIdx}")
    public ModelAndView bookDetail(@PathVariable Long bookIdx,HttpServletRequest request){
        Header<BookApiResponse> api = bookApiLogicService.read(bookIdx);
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/3_contents/book/book_detail").addObject("book",api.getData());
    }



    @GetMapping(path="/contents/movie")
    public ModelAndView movie(HttpServletRequest request){
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/3_contents/movie/movie").addObject("movies",movieApiLogicService.movieList());
    }
    @GetMapping(path="/contents/movie_edit/{movIdx}")
    public ModelAndView movieEdit(@PathVariable Long movIdx, HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        Header<MovieApiResponse> api = movieApiLogicService.read(movIdx);
        return loginInfo(request, "/3_contents/movie/movie_edit").addObject("movie",api.getData());
    }
    @GetMapping(path="/contents/movie_write")
    public ModelAndView movieWrite(HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/3_contents/movie/movie_write");
    }
    @GetMapping(path="/contents/movie_detail/{movIdx}")
    public ModelAndView movieDetail(@PathVariable Long movIdx, HttpServletRequest request){
        Header<MovieApiResponse> api = movieApiLogicService.read(movIdx);
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/3_contents/movie/movie_detail").addObject("movie",api.getData());
    }


    @GetMapping(path="/contents/tv")
    public ModelAndView tv(HttpServletRequest request){
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/3_contents/tv/tv").addObject("tvs",tvApiLogicService.tvList());
    }

    @GetMapping(path="/contents/tv_edit/{tvIdx}")
    public ModelAndView tvEdit(@PathVariable Long tvIdx,HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        Header<TvApiResponse> api = tvApiLogicService.read(tvIdx);
        return loginInfo(request, "/3_contents/tv/tv_edit").addObject("tv",api.getData());
    }
    @GetMapping(path="/contents/tv_write")
    public ModelAndView tvWrite(HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/3_contents/tv/tv_write");
    }
    @GetMapping(path="/contents/tv_detail/{tvIdx}")
    public ModelAndView tvDetail(@PathVariable Long tvIdx,HttpServletRequest request){
        Header<TvApiResponse> api = tvApiLogicService.read(tvIdx);
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/3_contents/tv/tv_detail").addObject("tv",api.getData());
    }



    @GetMapping(path="/contents/webtoon")
    public ModelAndView webtoon(HttpServletRequest request){
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/3_contents/webtoon/webtoon").addObject("webtoons",webtoonApiLogicService.webtoonList());

    }
    @GetMapping(path="/contents/webtoon_edit/{webIdx}")
    public ModelAndView webtoonEdit(@PathVariable Long webIdx,HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        Header<WebtoonApiResponse> api = webtoonApiLogicService.read(webIdx);
        return loginInfo(request, "/3_contents/webtoon/webtoon_edit").addObject("webtoon",api.getData());
    }
    @GetMapping(path="/contents/webtoon_write")
    public ModelAndView webtoonWrite(HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/3_contents/webtoon/webtoon_write");
    }
    @GetMapping(path="/contents/webtoon_detail/{webIdx}")
    public ModelAndView webtoonDetail(@PathVariable Long webIdx,HttpServletRequest request){
        Header<WebtoonApiResponse> api = webtoonApiLogicService.read(webIdx);
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/3_contents/webtoon/webtoon_detail").addObject("webtoon",api.getData());
    }



    private final ReportApiLogicService reportApiLogicService;
    private final ReportRepository reportRepository;


    @GetMapping(path="/comment/report_page")
    public ModelAndView report(HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }

        List<ReportDto> reportDtos = reportApiLogicService.findAllReport();
        List<ReportResponseDto> responseDtos = new ArrayList<>();
        for(ReportDto r : reportDtos){
            if(r.reportCommType().equals("comm")){
                String comm = "??????";
                responseDtos.add(ReportResponseDto.from(r, comm));
            }else{
                String recomm = "?????????";
                responseDtos.add(ReportResponseDto.from(r, recomm));
            }
        }
        //?????????, ??????????????? ?????? List ?????? (th:if??? ????????? ????????? BootStrap ??? CSS??? ??????)
        List<ReportResponseDto> waitList = new ArrayList<>();
        List<ReportResponseDto> completeList = new ArrayList<>();
        for(ReportResponseDto r : responseDtos){
            if(r.reportProcessing().equals("?????????")){
                waitList.add(r);
            }else{
                String[] status = r.reportProcessing().split(",");
                completeList.add(ReportResponseDto.from(r,status[0]));
            }
        }

        return loginInfo(request, "/4_comment/reported/report_page")
                .addObject("waitList", waitList)
                .addObject("completeList",completeList);
    }

    @GetMapping(path="/comment/reportdetail_comment/{reportId}")
    public ModelAndView reportdetailcomment(@PathVariable Long reportId, HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        Report report = reportRepository.getReferenceById(reportId);

        if(report.getReportProcessing() == null){
            report.setReportProcessing("?????????");
        }

        ReportDetailDto reportDetailDto = ReportDetailDto.from(report);

        return loginInfo(request, "/4_comment/reported/reportdetail_comment").addObject("report", reportDetailDto);
    }

    @GetMapping(path="/comment/reportdetail_reply/{reportId}")
    public ModelAndView reportdetailreply(@PathVariable Long reportId, HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }

        Report report = reportRepository.getReferenceById(reportId);

        if(report.getReportProcessing() == null){
            report.setReportProcessing("?????????");
        }

        ReportDetailDto reportDetailDto = ReportDetailDto.from(report);

        return loginInfo(request, "/4_comment/reported/reportdetail_reply").addObject("report", reportDetailDto);
    }

    // comment ????????? ??????
    private final CommentService commentService;
    @GetMapping(path="/comment/search_list")

    public String comment(HttpServletRequest request, ModelMap map){
        loginModelInfo(request,map);
        List<CommentResponse> comments = commentService.searchComments().stream().map(CommentResponse::from).toList();

        //??????????????????
        //???????????? : List<CommentResponse>
        //?????? content_type(movie, tv, book, webtoon)??? ???????????? ?????????????????? select?????? ????????? ????????? ?????????
        //List<CommentResponse> ??????
        comments = globalMethodService.getListTitle(comments);
        map.addAttribute("comments", comments);
        return "/4_comment/search/commentSearchList";
    }


    // commentDetail ?????? (??????, ?????????)
    final CommentRepository commentRepository;

    final RecommentRepository recommentRepository;

    final LikeRepository likeRepository;

    @GetMapping(path="/comment/{commentIdx}")
    public String commentdetail(@PathVariable Long commentIdx, ModelMap map, HttpServletRequest request){
        //????????? ???????????? ??????
        loginModelInfo(request,map);
        //Comment ???????????? ?????? idx ?????? ???????????? CommentResponseDto??? ??????
        Optional<Comment> comment = commentRepository.findById(commentIdx);
        CommentResponse commentResponse = CommentResponse.from(CommentDto.from(comment.get()));

        //??????????????????
        // ???????????? : CommentResponse
        // List<String>??????.get(0) ?????? : postUrl (????????? ????????? ???????????????)
        // List<String>??????.get(1) ?????? : title (????????? ??????)
        List<String> str = globalMethodService.getPostUrlAndTitle(commentResponse);

        //???????????? - ?????? ???????????? ?????????
        List<RecommentResponseDto> recommentResponseDtoList = new ArrayList<>();
        List<Recomment> recommentList = comment.get().getRecommentList();
        for(Recomment r : recommentList){
            LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
            LocalDateTime regDay = r.getRecommRegDate().truncatedTo(ChronoUnit.DAYS);
            Long sicha = ChronoUnit.DAYS.between(regDay,today);
            String dayAgo = "";
            if(sicha > 0){
                dayAgo = sicha + "??????";
            }else{
                dayAgo = "??????";
            }
            recommentResponseDtoList.add(RecommentResponseDto.dayAgo(RecommentDto.from(r),dayAgo));
            System.out.println(RecommentResponseDto.dayAgo(RecommentDto.from(r),dayAgo));
        }

        commentResponse = CommentResponse.inserPosterUrl(commentResponse, str.get(0));  //???????????? ????????? postUrl
        commentResponse = CommentResponse.insertTitle(commentResponse, str.get(1)); //???????????? ????????? title
        map.addAttribute("comment", commentResponse);
        map.addAttribute("recomment", recommentResponseDtoList);

        return "/4_comment/search/commentSearchDetail";
    }
    @GetMapping(path="/character_detail/{perIdx}")
    public ModelAndView characterdetail(@PathVariable Long perIdx, HttpServletRequest request){
        Header<CharacterApiResponse> api = characterApiLogicService.read(perIdx);
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/5_character/characterdetail").addObject("character",api.getData());
    }
    @GetMapping(path="/character_manage")
    public ModelAndView charactermanage(HttpServletRequest request){
// ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        System.out.println("??????????????????????????? ??????????????????");

        return loginInfo(request, "/5_character/charactermanage").addObject("characters",characterApiLogicService.characterList());

    }

    //?????? ??????
    @GetMapping(path="/character_register")
    public ModelAndView characterregister(HttpServletRequest request){
// ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/5_character/characterregister");
    }
    @GetMapping(path="/character_modify/{perIdx}")
    public ModelAndView charactermodify(@PathVariable Long perIdx,HttpServletRequest request){
// ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        Header<CharacterApiResponse> api = characterApiLogicService.read(perIdx);
        return loginInfo(request, "/5_character/charactermodify").addObject("character",api.getData());
    }

    // ?????? ?????????
    final UserRepository userRepository;
    @GetMapping(path="/member/{userIdx}")
    public String memberdetail(@PathVariable Long userIdx, ModelMap map, HttpServletRequest request){
        loginModelInfo(request,map);
        Optional<User> user = userRepository.findById(userIdx);
        UserResponse userResponse = UserResponse.from(UserDto.from(user.get()));
        map.addAttribute("user", userResponse);
        return "/6_member/memberdetail";
    }

    // ?????? ?????????
    final UserService userService;
    private final MovieRepository movieRepository;
    private final TvRepository tvRepository;
    private final BookRepository bookRepository;
    private final WebtoonRepository webtoonRepository;

    @GetMapping(path="/member")
    public String membermanage(HttpServletRequest request, ModelMap map){
        loginModelInfo(request,map);
        List<UserResponse> users = userService.searchUsers().stream().map(UserResponse::from).toList();
        map.addAttribute("users", users);
        return "/6_member/membermanage";
    }

    // ?????? ??????????????? ??????
    @GetMapping("/member/{findAllReportx}/{userType}")
    public String updateUser(
             @PathVariable Long findAllReportx,
             @PathVariable String userType,
             ModelMap map,
             HttpServletRequest request){
        loginModelInfo(request,map);
        Optional<User> user = userRepository.findById(findAllReportx);
        user.ifPresent(
                selectUser -> {
                    selectUser.setUserType(userType);
                    userRepository.save(selectUser);
                }
        );
        return "redirect:/member/"+findAllReportx;
    }


    //??????
    @GetMapping(path="/advertisement")
    public ModelAndView admain(HttpServletRequest request){
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/7_advertisement/Advertisement").addObject("advertises",advertiseApiLogicService.advertiseList());
    }

    @GetMapping(path="/advertisement_write")
    public ModelAndView adwrite(HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/7_advertisement/Advertisement_Write");
    }


    @GetMapping(path="/advertisement_edit/{adIdx}")
    public ModelAndView adedit(@PathVariable Long adIdx, HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        Header<AdvertiseApiResponse> api = advertiseApiLogicService.read(adIdx);
        return loginInfo(request, "/7_advertisement/Advertisement_Edit").addObject("advertise",api.getData());
    }


    @GetMapping(path="/advertisement_view/{adIdx}")
    public ModelAndView adview(@PathVariable Long adIdx, HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        Header<AdvertiseApiResponse> api = advertiseApiLogicService.read(adIdx);
        return loginInfo(request, "/7_advertisement/Advertisement_View").addObject("advertise",api.getData());
    }


    @GetMapping(path="/hradmin/{adminIdx}")
    public String hradminaccountdetail(@PathVariable Long adminIdx, ModelMap map, HttpServletRequest request){
        loginModelInfo(request,map);
        //idx??? ??????????????? ??????????????? ???????????? map??? ???????????? ??????
        Optional<AdminUser> adminUser = adminRepository.findById(adminIdx);
        AdminUserResponse adminUserResponse = AdminUserResponse.from(AdminUserDto.from(adminUser.get()));
        map.addAttribute("adminUser", adminUserResponse);
        return "/8_admin/hradmin/accountdetail";
    }
    @GetMapping(path="/hradmin/createaccount")
    public ModelAndView hradmincreateaccount(HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/8_admin/hradmin/createaccount");
    }
    @GetMapping(path="/hradmin/modifyaccount")
    public ModelAndView hradminmodifyaccount(HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        HttpSession session = request.getSession();
        Header<AdminApiResponse> api = adminApiLogicService.read((Long)session.getAttribute("adminIdx"));
        return loginInfo(request, "/8_admin/admin/Myinfo").addObject("myinfo",api.getData());
    }

    @Autowired
    private final AdminService adminService;
    private final AdminRepository adminRepository;

    @GetMapping(path="/hradmin/searchaccount")
    public String hradminsearchaccount(HttpServletRequest request, ModelMap map){
        //????????? ???????????? ??????!
        loginModelInfo(request,map);

        // admin?????? ????????? ??????!
        List<AdminUserResponse> admins = adminService.findAllReport().stream().map(AdminUserResponse::from).toList();
        map.addAttribute("admins",admins);
        return "/8_admin/hradmin/searchaccount";
    }

    @GetMapping(path="/admin_myinfo")
    public ModelAndView myinfo(HttpServletRequest request){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        HttpSession session = request.getSession();
        Header<AdminApiResponse> api = adminApiLogicService.read((Long)session.getAttribute("adminIdx"));
        return loginInfo(request, "/8_admin/admin/Myinfo").addObject("myinfo",api.getData());
    }

    @GetMapping(path="/admin_myinfomodify/{adminIdx}")
    public ModelAndView myinfomodify(HttpServletRequest request,@PathVariable Long adminIdx){
        // ????????? Check ??????!
        ModelAndView loginCheck = loginCheck(request);
        if(loginCheck != null){
            return loginCheck;
        }
        return loginInfo(request, "/8_admin/admin/Myinfomodify").addObject("adminIdx",adminIdx);
    }

    @GetMapping(path="/hradmin/updateaccount/{adminIdx}")
    public String updateaccount(@PathVariable Long adminIdx, HttpServletRequest request, ModelMap map){
        loginModelInfo(request,map);
        AdminUser adminUser = adminRepository.getReferenceById(adminIdx);
        AdminUserResponse adminUserResponse = AdminUserResponse.from(AdminUserDto.from(adminUser));
        map.addAttribute("adminUser", adminUserResponse);
        return "/8_admin/hradmin/updateaccount";
    }

}
