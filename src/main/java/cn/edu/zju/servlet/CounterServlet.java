package cn.edu.zju.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Week 3 Practical - Visitor Counter Servlet
 *
 * 演示两种计数器的 Scope / 生命周期差异：
 *
 * A) Session（会话）计数器
 *    - 存储在 HttpSession 的 attribute 中（"sessionCounter"）
 *    - 生命周期：从用户第一次访问开始，到 session 超时或浏览器关闭（Cookie 丢失）时结束
 *    - 每个用户有自己独立的 session，互不影响
 *
 * B) Application（全局）计数器
 *    - 存储在 ServletContext 的 attribute 中（"globalCounter"），使用 AtomicInteger 保证线程安全
 *    - 生命周期：与 Web 应用同寿，从 Tomcat 启动到 Tomcat 停止（重启则清零）
 *    - 所有用户共享同一个 ServletContext，因此全局计数是累计所有用户访问次数
 *
 * ===== 为何不能用 Servlet 实例变量来做计数器 =====
 * 错误示例：private int counter = 0;
 * 原因：Tomcat 对每个 Servlet 只创建「一个实例」（默认单例），所有并发请求
 *       都会共享该实例的字段。若用普通 int 字段，多线程同时 counter++ 会产生
 *       竞态条件（Race Condition），导致计数不准确，且该字段也没有 session 语义
 *       ——关闭浏览器后计数不会重置，违背了 session 计数的设计目的。
 *
 * 映射路径：GET /counter
 * 通过 @WebServlet 注解注册（与项目现有的 web.xml 注册方式并存；
 * 因本项目 web.xml 使用 Servlet 2.3 DTD，不支持注解扫描，
 * 所以这里改用 Servlet 3.0 注解，同时将 web.xml 升级为 3.1 版本）。
 */
@WebServlet("/counter")
public class CounterServlet extends HttpServlet {

    /**
     * 在 Servlet 初始化时，把全局计数器（AtomicInteger）放入 ServletContext。
     * 使用 init() 而非 static 块，是因为 ServletContext 需通过 getServletContext() 获取。
     */
    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext ctx = getServletContext();
        // 只有第一次初始化时才放入；热重部署时 ServletContext 属性可能已存在
        if (ctx.getAttribute("globalCounter") == null) {
            ctx.setAttribute("globalCounter", new AtomicInteger(0));
        }
    }

    /**
     * 处理 GET /counter 请求
     * 步骤：
     *   1. 获取（或创建）当前用户的 HttpSession，读取 sessionCounter，+1 后写回
     *   2. 获取全局 AtomicInteger，调用 incrementAndGet()（线程安全的原子递增）
     *   3. 用 PrintWriter 输出 HTML 页面
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ---- A: Session 计数器 ----
        // getSession(true) => 不存在时自动创建新 session
        HttpSession session = req.getSession(true);
        Integer sessionCount = (Integer) session.getAttribute("sessionCounter");
        if (sessionCount == null) {
            sessionCount = 0;
        }
        sessionCount++;
        session.setAttribute("sessionCounter", sessionCount);

        // ---- B: 全局（Application）计数器 ----
        // AtomicInteger.incrementAndGet() 是原子操作，无需额外 synchronized
        ServletContext ctx = getServletContext();
        AtomicInteger globalCounter = (AtomicInteger) ctx.getAttribute("globalCounter");
        if (globalCounter == null) {
            // 防御性初始化（理论上 init() 已放入，这里保险）
            globalCounter = new AtomicInteger(0);
            ctx.setAttribute("globalCounter", globalCounter);
        }
        int globalCount = globalCounter.incrementAndGet();

        // ---- 输出 HTML ----
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("  <meta charset='UTF-8'>");
        out.println("  <meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("  <title>Visitor Counter - Week 3 Practical</title>");
        out.println("  <style>");
        out.println("    body { font-family: Arial, sans-serif; max-width: 700px; margin: 60px auto; padding: 0 20px; }");
        out.println("    h1 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 8px; }");
        out.println("    .card { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 8px;");
        out.println("            padding: 20px; margin: 16px 0; }");
        out.println("    .count { font-size: 2.5em; font-weight: bold; color: #007bff; }");
        out.println("    .label { font-size: 0.9em; color: #6c757d; margin-top: 4px; }");
        out.println("    .explanation { background: #fff3cd; border: 1px solid #ffc107; border-radius: 6px;");
        out.println("                   padding: 14px 18px; margin-top: 24px; font-size: 0.93em; }");
        out.println("    a.btn { display: inline-block; margin-top: 20px; padding: 10px 22px;");
        out.println("            background: #007bff; color: #fff; border-radius: 5px;");
        out.println("            text-decoration: none; }");
        out.println("    a.btn:hover { background: #0056b3; }");
        out.println("    a.home { display: inline-block; margin-top: 10px; margin-left: 12px;");
        out.println("             color: #007bff; text-decoration: none; font-size: 0.95em; }");
        out.println("  </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("  <h1>&#128202; Visitor Counter Demo</h1>");
        out.println("  <p><em>Week 3 Practical &mdash; Variable Scope &amp; Servlet Lifecycle</em></p>");

        // Session 计数卡片
        out.println("  <div class='card'>");
        out.println("    <div class='count'>" + sessionCount + "</div>");
        out.println("    <div><strong>Session Visit Count</strong></div>");
        out.println("    <div class='label'>&#128260; Resets after browser close &mdash; stored in <code>HttpSession</code></div>");
        out.println("  </div>");

        // Global 计数卡片
        out.println("  <div class='card'>");
        out.println("    <div class='count'>" + globalCount + "</div>");
        out.println("    <div><strong>Global Visit Count</strong></div>");
        out.println("    <div class='label'>&#127758; Persists until server restart &mdash; stored in <code>ServletContext</code> as <code>AtomicInteger</code></div>");
        out.println("  </div>");

        // 解释说明
        out.println("  <div class='explanation'>");
        out.println("    <strong>Why do they behave differently?</strong><br>");
        out.println("    <ul>");
        out.println("      <li><strong>Session scope</strong>: Each browser/user gets its own <code>HttpSession</code>.");
        out.println("          When you close the browser, the session cookie is lost; the next visit creates a new session,");
        out.println("          so the session counter resets to 1.</li>");
        out.println("      <li><strong>Application scope</strong>: <code>ServletContext</code> is shared across ALL users");
        out.println("          and survives browser restarts. It is only destroyed when Tomcat stops,");
        out.println("          so the global counter keeps growing.</li>");
        out.println("      <li><strong>Why NOT use an instance field <code>private int counter</code>?</strong><br>");
        out.println("          Tomcat creates only ONE Servlet instance (singleton). Multiple concurrent requests");
        out.println("          share it, causing race conditions on a plain <code>int</code>.");
        out.println("          Also, an instance field has no session semantics &mdash; it never resets on browser close.</li>");
        out.println("    </ul>");
        out.println("  </div>");

        out.println("  <a class='btn' href='" + req.getRequestURI() + "'>&#128260; Refresh to increment</a>");
        out.println("  <a class='home' href='" + req.getContextPath() + "/'>&#8592; Back to Dashboard</a>");
        out.println("</body>");
        out.println("</html>");
    }
}