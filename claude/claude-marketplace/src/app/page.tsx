import {
  Beer,
  Brain,
  Star,
  Sparkles,
  ChevronRight,
  Heart,
  BookOpen,
  Users,
  Utensils,
  CloudSun,
  BarChart3,
  Gift,
  ArrowRight,
  Check,
  Quote,
} from "lucide-react";

export default function Home() {
  return (
    <div
      style={{ backgroundColor: "var(--bg-deep)", color: "var(--text-primary)" }}
      className="min-h-screen overflow-x-hidden"
    >
      {/* ─── 내비게이션 ─── */}
      <Nav />

      {/* ─── 히어로 ─── */}
      <HeroSection />

      {/* ─── 통계 바 ─── */}
      <StatsBar />

      {/* ─── 핵심 기능 ─── */}
      <FeaturesSection />

      {/* ─── AI 추천 엔진 ─── */}
      <AISection />

      {/* ─── 페어링 ─── */}
      <PairingSection />

      {/* ─── 사용자 리뷰 ─── */}
      <ReviewsSection />

      {/* ─── 온보딩 CTA ─── */}
      <CTASection />

      {/* ─── 푸터 ─── */}
      <Footer />
    </div>
  );
}

/* ═══════════════════════════════════════════
   내비게이션
═══════════════════════════════════════════ */
function Nav() {
  return (
    <nav
      style={{
        backgroundColor: "rgba(9,6,3,0.85)",
        borderBottom: "1px solid var(--border-subtle)",
        backdropFilter: "blur(20px)",
        WebkitBackdropFilter: "blur(20px)",
      }}
      className="fixed top-0 left-0 right-0 z-50"
    >
      <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
        {/* 로고 */}
        <div className="flex items-center gap-2.5">
          <div
            style={{ backgroundColor: "var(--amber)" }}
            className="w-8 h-8 rounded-lg flex items-center justify-center"
          >
            <Beer className="w-4.5 h-4.5 text-black" strokeWidth={2.5} />
          </div>
          <span
            style={{ fontFamily: "var(--font-playfair)", color: "var(--text-primary)" }}
            className="text-xl font-bold tracking-tight"
          >
            BrewPick
          </span>
        </div>

        {/* 메뉴 */}
        <div className="hidden md:flex items-center gap-8">
          {["기능", "AI 추천", "페어링", "커뮤니티"].map((item) => (
            <a
              key={item}
              href="#"
              style={{ color: "var(--text-muted)" }}
              className="text-sm font-medium transition-colors hover:text-amber-400"
            >
              {item}
            </a>
          ))}
        </div>

        {/* CTA */}
        <a
          href="#"
          className="btn-shimmer text-black text-sm font-semibold px-5 py-2.5 rounded-full"
        >
          무료로 시작하기
        </a>
      </div>
    </nav>
  );
}

/* ═══════════════════════════════════════════
   히어로 섹션
═══════════════════════════════════════════ */
function HeroSection() {
  return (
    <section className="relative min-h-screen flex items-center overflow-hidden pt-16">
      {/* 배경 그라디언트 오브 */}
      <div
        className="absolute animate-float-slow animate-pulse-glow pointer-events-none"
        style={{
          width: "700px",
          height: "700px",
          borderRadius: "50%",
          background: "radial-gradient(circle, rgba(217,119,6,0.22) 0%, rgba(180,83,9,0.08) 50%, transparent 70%)",
          top: "-100px",
          right: "-200px",
          filter: "blur(40px)",
        }}
      />
      <div
        className="absolute animate-float-medium animate-pulse-glow pointer-events-none delay-400"
        style={{
          width: "500px",
          height: "500px",
          borderRadius: "50%",
          background: "radial-gradient(circle, rgba(245,158,11,0.15) 0%, transparent 65%)",
          bottom: "0px",
          left: "-100px",
          filter: "blur(50px)",
        }}
      />
      <div
        className="absolute pointer-events-none"
        style={{
          width: "300px",
          height: "300px",
          borderRadius: "50%",
          background: "radial-gradient(circle, rgba(251,191,36,0.1) 0%, transparent 65%)",
          top: "40%",
          left: "45%",
          filter: "blur(60px)",
          animation: "float-slow 15s ease-in-out infinite reverse",
        }}
      />

      {/* 링 데코레이션 */}
      <div
        className="absolute animate-spin-slow pointer-events-none opacity-10"
        style={{
          width: "600px",
          height: "600px",
          border: "1px solid var(--amber)",
          borderRadius: "50%",
          top: "50%",
          right: "-200px",
          transform: "translateY(-50%)",
        }}
      />
      <div
        className="absolute pointer-events-none opacity-5"
        style={{
          width: "400px",
          height: "400px",
          border: "1px solid var(--amber-bright)",
          borderRadius: "50%",
          top: "50%",
          right: "-100px",
          transform: "translateY(-50%)",
          animation: "spin-slow 30s linear infinite reverse",
        }}
      />

      <div className="relative max-w-7xl mx-auto px-6 py-32 grid lg:grid-cols-2 gap-16 items-center">
        {/* 좌측 텍스트 */}
        <div>
          {/* 배지 */}
          <div
            className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-semibold mb-8 animate-fade-up"
            style={{
              background: "rgba(217,119,6,0.12)",
              border: "1px solid var(--border-amber)",
              color: "var(--amber-glow)",
              letterSpacing: "0.08em",
            }}
          >
            <Sparkles className="w-3 h-3" />
            AI 기반 맥주 큐레이션 플랫폼
          </div>

          {/* 헤드라인 */}
          <h1
            className="animate-fade-up delay-100"
            style={{
              fontFamily: "var(--font-playfair)",
              fontSize: "clamp(2.8rem, 6vw, 5rem)",
              fontWeight: 700,
              lineHeight: 1.1,
              letterSpacing: "-0.02em",
              color: "var(--text-primary)",
            }}
          >
            당신만의
            <br />
            <span className="text-gradient-amber">완벽한 맥주</span>를
            <br />
            발견하세요
          </h1>

          {/* 서브헤드 */}
          <p
            className="mt-6 text-lg leading-relaxed animate-fade-up delay-200"
            style={{
              color: "var(--text-secondary)",
              maxWidth: "480px",
            }}
          >
            수백 종의 크래프트 맥주 중 어떤 걸 골라야 할지 고민하지 마세요.
            AI가 취향, 상황, 페어링 음식을 분석해 지금 이 순간 최적의 맥주를 추천합니다.
          </p>

          {/* CTA 버튼 */}
          <div className="flex flex-col sm:flex-row gap-4 mt-10 animate-fade-up delay-300">
            <a
              href="#"
              className="btn-shimmer text-black font-semibold px-8 py-4 rounded-full text-base flex items-center justify-center gap-2"
            >
              취향 분석 시작하기
              <ArrowRight className="w-4 h-4" />
            </a>
            <a
              href="#"
              className="btn-outline-amber px-8 py-4 rounded-full text-base font-medium flex items-center justify-center gap-2"
            >
              <Beer className="w-4 h-4" />
              맥주 탐색하기
            </a>
          </div>

          {/* 미니 소셜 프루프 */}
          <div className="flex items-center gap-4 mt-8 animate-fade-up delay-400">
            <div className="flex -space-x-2">
              {["🧑", "👩", "🧔", "👱"].map((emoji, i) => (
                <div
                  key={i}
                  className="w-8 h-8 rounded-full flex items-center justify-center text-sm border-2"
                  style={{
                    backgroundColor: "var(--bg-card)",
                    borderColor: "var(--bg-deep)",
                  }}
                >
                  {emoji}
                </div>
              ))}
            </div>
            <div>
              <div className="flex items-center gap-1">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                ))}
              </div>
              <p className="text-xs mt-0.5" style={{ color: "var(--text-muted)" }}>
                <strong style={{ color: "var(--text-secondary)" }}>4.9/5</strong> · 2,300+ 리뷰
              </p>
            </div>
          </div>
        </div>

        {/* 우측 — 비주얼 카드 */}
        <div className="relative animate-scale-in delay-300 hidden lg:block">
          <HeroVisual />
        </div>
      </div>
    </section>
  );
}

function HeroVisual() {
  const beers = [
    { name: "한강 세션 IPA", brewery: "핸드앤몰트", abv: "4.5%", style: "IPA", match: 97, color: "#d97706" },
    { name: "Blanc Wheat", brewery: "제주 맥주", abv: "4.3%", style: "Wheat", match: 92, color: "#a78bfa" },
    { name: "Stout Obsidian", brewery: "더 핸드", abv: "7.2%", style: "Stout", match: 85, color: "#374151" },
  ];

  return (
    <div className="relative">
      {/* 메인 카드 */}
      <div
        className="rounded-3xl p-6 shadow-2xl"
        style={{
          backgroundColor: "var(--bg-card)",
          border: "1px solid var(--border-amber)",
          boxShadow: "0 25px 80px rgba(0,0,0,0.6), 0 0 60px rgba(217,119,6,0.1)",
        }}
      >
        <div className="flex items-center justify-between mb-5">
          <div>
            <p className="text-xs font-semibold tracking-widest uppercase" style={{ color: "var(--amber)" }}>
              AI 추천 결과
            </p>
            <p className="text-sm mt-1" style={{ color: "var(--text-muted)" }}>
              "치킨에 어울리는 시원한 맥주"
            </p>
          </div>
          <div
            className="w-9 h-9 rounded-xl flex items-center justify-center"
            style={{ backgroundColor: "rgba(217,119,6,0.15)" }}
          >
            <Sparkles className="w-4 h-4" style={{ color: "var(--amber)" }} />
          </div>
        </div>

        <div className="space-y-3">
          {beers.map((beer, i) => (
            <div
              key={i}
              className="flex items-center gap-4 p-3.5 rounded-2xl transition-all"
              style={{
                backgroundColor: i === 0 ? "rgba(217,119,6,0.1)" : "rgba(255,255,255,0.03)",
                border: i === 0 ? "1px solid rgba(217,119,6,0.3)" : "1px solid transparent",
              }}
            >
              {/* 색상 아이콘 */}
              <div
                className="w-11 h-11 rounded-xl flex items-center justify-center flex-shrink-0"
                style={{ backgroundColor: beer.color + "22", border: `1px solid ${beer.color}44` }}
              >
                <Beer className="w-5 h-5" style={{ color: beer.color }} />
              </div>

              <div className="flex-1 min-w-0">
                <p className="font-semibold text-sm truncate" style={{ color: "var(--text-primary)" }}>
                  {beer.name}
                </p>
                <p className="text-xs mt-0.5" style={{ color: "var(--text-muted)" }}>
                  {beer.brewery} · {beer.abv} · {beer.style}
                </p>
              </div>

              <div className="text-right flex-shrink-0">
                <p
                  className="text-sm font-bold"
                  style={{ color: i === 0 ? "var(--amber-glow)" : "var(--text-muted)" }}
                >
                  {beer.match}%
                </p>
                <p className="text-xs" style={{ color: "var(--text-muted)" }}>
                  매치
                </p>
              </div>
            </div>
          ))}
        </div>

        {/* 하단 맛 프로필 */}
        <div
          className="mt-5 p-4 rounded-2xl"
          style={{ backgroundColor: "rgba(255,255,255,0.03)", border: "1px solid var(--border-subtle)" }}
        >
          <p className="text-xs font-semibold mb-3" style={{ color: "var(--text-muted)" }}>
            맛 프로필 — 한강 세션 IPA
          </p>
          <div className="grid grid-cols-3 gap-3">
            {[
              { label: "홉향", value: 85 },
              { label: "청량감", value: 78 },
              { label: "쓴맛", value: 52 },
              { label: "단맛", value: 20 },
              { label: "과일향", value: 65 },
              { label: "몰트", value: 38 },
            ].map((item) => (
              <div key={item.label}>
                <div className="flex justify-between mb-1">
                  <span className="text-xs" style={{ color: "var(--text-muted)" }}>
                    {item.label}
                  </span>
                  <span className="text-xs font-medium" style={{ color: "var(--amber)" }}>
                    {item.value}
                  </span>
                </div>
                <div
                  className="h-1 rounded-full overflow-hidden"
                  style={{ backgroundColor: "rgba(255,255,255,0.06)" }}
                >
                  <div
                    className="h-full rounded-full"
                    style={{
                      width: `${item.value}%`,
                      background: "linear-gradient(90deg, var(--amber) 0%, var(--amber-glow) 100%)",
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 플로팅 배지들 */}
      <div
        className="absolute -top-4 -left-6 px-3 py-2 rounded-xl text-xs font-semibold animate-float-slow"
        style={{
          backgroundColor: "var(--bg-card)",
          border: "1px solid var(--border-amber)",
          color: "var(--amber-glow)",
          boxShadow: "0 8px 24px rgba(0,0,0,0.4)",
        }}
      >
        🍗 치킨 + IPA = 완벽
      </div>

      <div
        className="absolute -bottom-4 -right-6 px-3 py-2 rounded-xl text-xs font-semibold animate-float-medium delay-300"
        style={{
          backgroundColor: "var(--bg-card)",
          border: "1px solid rgba(167,139,250,0.3)",
          color: "#a78bfa",
          boxShadow: "0 8px 24px rgba(0,0,0,0.4)",
        }}
      >
        ✨ 500+ 크래프트 맥주
      </div>
    </div>
  );
}

/* ═══════════════════════════════════════════
   통계 바
═══════════════════════════════════════════ */
function StatsBar() {
  const stats = [
    { value: "500+", label: "크래프트 맥주 DB" },
    { value: "97%", label: "추천 만족도" },
    { value: "50K", label: "월간 활성 사용자" },
    { value: "3분", label: "취향 온보딩 완료" },
  ];

  return (
    <div
      className="relative"
      style={{ borderTop: "1px solid var(--border-subtle)", borderBottom: "1px solid var(--border-subtle)" }}
    >
      <div
        className="absolute inset-0 opacity-30"
        style={{
          background: "linear-gradient(90deg, transparent, rgba(217,119,6,0.04), transparent)",
        }}
      />
      <div className="max-w-7xl mx-auto px-6 py-8">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
          {stats.map((stat, i) => (
            <div key={i} className="text-center">
              <p
                className="text-3xl font-bold"
                style={{
                  fontFamily: "var(--font-playfair)",
                  color: "var(--amber-glow)",
                }}
              >
                {stat.value}
              </p>
              <p className="text-sm mt-1" style={{ color: "var(--text-muted)" }}>
                {stat.label}
              </p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

/* ═══════════════════════════════════════════
   핵심 기능 섹션
═══════════════════════════════════════════ */
function FeaturesSection() {
  const features = [
    {
      icon: Brain,
      title: "AI 취향 분석",
      description:
        "5~7개 질문으로 당신의 맥주 취향 DNA를 파악합니다. 쓴맛, 향, 도수, 스타일 선호도를 분석해 즉각적인 맞춤 프로필을 생성합니다.",
      tag: "온보딩",
      color: "#8b5cf6",
    },
    {
      icon: Sparkles,
      title: "상황별 맥락 추천",
      description:
        "\"혼자 영화 보며\" \"치킨이랑\" \"더운 여름날\" — 자연어로 입력하면 Claude AI가 맥락을 이해해 지금 이 순간 최적의 맥주를 찾아드립니다.",
      tag: "AI 추천",
      color: "var(--amber)",
    },
    {
      icon: BookOpen,
      title: "깊이 있는 맥주 정보",
      description:
        "스타일, 원산지, ABV, IBU부터 맛 프로필 레이더 차트, 서빙 온도, 추천 글라스까지. 맥주 입문자도 마니아도 만족하는 정보 깊이를 제공합니다.",
      tag: "상세 정보",
      color: "#10b981",
    },
    {
      icon: Heart,
      title: "나만의 맥주 컬렉션",
      description:
        "마신 맥주를 기록하고, 위시리스트를 저장하고, 테마별 큐레이션을 만드세요. 연말에는 당신만의 맥주 리포트가 완성됩니다.",
      tag: "컬렉션",
      color: "#f43f5e",
    },
    {
      icon: Utensils,
      title: "음식 페어링 가이드",
      description:
        "삼겹살엔 필스너, 치킨엔 IPA, 스테이크엔 포터. 음식을 선택하면 최고의 궁합 맥주 목록과 그 이유를 알려드립니다.",
      tag: "페어링",
      color: "#f59e0b",
    },
    {
      icon: Users,
      title: "커뮤니티 리뷰",
      description:
        "비슷한 취향의 사용자 리뷰를 우선적으로 보여주는 스마트 피드. 팔로우 기능으로 신뢰할 수 있는 맥주 큐레이터를 구독하세요.",
      tag: "커뮤니티",
      color: "#06b6d4",
    },
  ];

  return (
    <section className="py-28 px-6 relative">
      <div className="max-w-7xl mx-auto">
        {/* 섹션 헤더 */}
        <div className="text-center mb-16">
          <p
            className="text-xs font-semibold tracking-[0.2em] uppercase mb-4"
            style={{ color: "var(--amber)" }}
          >
            핵심 기능
          </p>
          <h2
            className="text-4xl md:text-5xl font-bold"
            style={{ fontFamily: "var(--font-playfair)", color: "var(--text-primary)" }}
          >
            맥주 탐색의
            <br />
            <span className="text-gradient-amber">모든 것</span>을 담았습니다
          </h2>
          <p className="mt-5 text-lg max-w-xl mx-auto" style={{ color: "var(--text-secondary)" }}>
            입문자부터 마니아까지, 혼자 즐기거나 선물을 고를 때도 BrewPick이 함께합니다
          </p>
        </div>

        {/* 기능 그리드 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {features.map((feature, i) => {
            const Icon = feature.icon;
            return (
              <div
                key={i}
                className="card-hover rounded-2xl p-6"
                style={{
                  backgroundColor: "var(--bg-card)",
                  border: "1px solid var(--border-subtle)",
                }}
              >
                <div className="flex items-start gap-4">
                  <div
                    className="w-11 h-11 rounded-xl flex items-center justify-center flex-shrink-0 mt-0.5"
                    style={{
                      backgroundColor: feature.color + "18",
                      border: `1px solid ${feature.color}30`,
                    }}
                  >
                    <Icon className="w-5 h-5" style={{ color: feature.color }} />
                  </div>
                  <div>
                    <div className="flex items-center gap-2 mb-2">
                      <h3
                        className="font-semibold text-base"
                        style={{ color: "var(--text-primary)" }}
                      >
                        {feature.title}
                      </h3>
                      <span
                        className="text-xs px-2 py-0.5 rounded-full font-medium"
                        style={{
                          backgroundColor: feature.color + "18",
                          color: feature.color,
                        }}
                      >
                        {feature.tag}
                      </span>
                    </div>
                    <p className="text-sm leading-relaxed" style={{ color: "var(--text-muted)" }}>
                      {feature.description}
                    </p>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════
   AI 추천 엔진 섹션
═══════════════════════════════════════════ */
function AISection() {
  const conversations = [
    {
      role: "user",
      text: "오늘 퇴근 후 집에서 혼자 피자 먹으면서 가볍게 마시기 좋은 맥주 추천해줘",
    },
    {
      role: "ai",
      text: "피자와 가볍게 즐기기 딱 좋은 American Pale Ale을 추천드려요! 홉의 과일향이 피자의 치즈와 환상의 궁합을 이루고, 4.5% 도수로 편안하게 즐길 수 있어요.",
      beers: ["제주 위트 에일", "핸드앤몰트 세션 IPA", "크래프트브로스 페일에일"],
    },
  ];

  const advantages = [
    "자연어로 자유롭게 원하는 상황 설명",
    "날씨 · 기분 · 음식 맥락 통합 분석",
    "500+ 맥주 DB 기반 정밀 매칭",
    "비슷한 취향 사용자 데이터 반영",
  ];

  return (
    <section
      className="py-28 px-6 relative overflow-hidden"
      style={{ backgroundColor: "var(--bg-surface)" }}
    >
      {/* 배경 글로우 */}
      <div
        className="absolute pointer-events-none"
        style={{
          width: "600px",
          height: "600px",
          borderRadius: "50%",
          background: "radial-gradient(circle, rgba(217,119,6,0.08) 0%, transparent 70%)",
          top: "50%",
          right: "-100px",
          transform: "translateY(-50%)",
          filter: "blur(60px)",
        }}
      />

      <div className="max-w-7xl mx-auto relative">
        <div className="grid lg:grid-cols-2 gap-16 items-center">
          {/* 좌측: AI 채팅 UI 목업 */}
          <div
            className="rounded-3xl overflow-hidden shadow-2xl"
            style={{
              backgroundColor: "var(--bg-card)",
              border: "1px solid var(--border-amber)",
              boxShadow: "0 25px 80px rgba(0,0,0,0.5)",
            }}
          >
            {/* 채팅 헤더 */}
            <div
              className="px-5 py-4 flex items-center gap-3"
              style={{ borderBottom: "1px solid var(--border-subtle)" }}
            >
              <div
                className="w-8 h-8 rounded-lg flex items-center justify-center"
                style={{ backgroundColor: "rgba(217,119,6,0.15)" }}
              >
                <Sparkles className="w-4 h-4" style={{ color: "var(--amber)" }} />
              </div>
              <div>
                <p className="text-sm font-semibold" style={{ color: "var(--text-primary)" }}>
                  BrewPick AI
                </p>
                <p className="text-xs" style={{ color: "var(--text-muted)" }}>
                  Claude API 기반 추천 엔진
                </p>
              </div>
              <div
                className="ml-auto w-2 h-2 rounded-full"
                style={{ backgroundColor: "#10b981" }}
              />
            </div>

            {/* 메시지 */}
            <div className="p-5 space-y-4">
              {conversations.map((msg, i) => (
                <div key={i} className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}>
                  <div
                    className="max-w-xs rounded-2xl px-4 py-3 text-sm"
                    style={{
                      backgroundColor:
                        msg.role === "user"
                          ? "rgba(217,119,6,0.15)"
                          : "rgba(255,255,255,0.05)",
                      border:
                        msg.role === "user"
                          ? "1px solid var(--border-amber)"
                          : "1px solid var(--border-subtle)",
                      color: "var(--text-secondary)",
                    }}
                  >
                    <p className="leading-relaxed">{msg.text}</p>
                    {msg.beers && (
                      <div className="mt-3 space-y-2">
                        {msg.beers.map((beer, j) => (
                          <div
                            key={j}
                            className="flex items-center gap-2 px-3 py-2 rounded-xl"
                            style={{
                              backgroundColor: "rgba(217,119,6,0.1)",
                              border: "1px solid var(--border-amber)",
                            }}
                          >
                            <Beer className="w-3.5 h-3.5 flex-shrink-0" style={{ color: "var(--amber)" }} />
                            <span className="text-xs font-medium" style={{ color: "var(--text-primary)" }}>
                              {beer}
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              ))}

              {/* 타이핑 인디케이터 */}
              <div className="flex justify-start">
                <div
                  className="px-4 py-3 rounded-2xl flex items-center gap-1.5"
                  style={{
                    backgroundColor: "rgba(255,255,255,0.05)",
                    border: "1px solid var(--border-subtle)",
                  }}
                >
                  {[0, 1, 2].map((i) => (
                    <div
                      key={i}
                      className="w-1.5 h-1.5 rounded-full"
                      style={{
                        backgroundColor: "var(--amber)",
                        animation: `pulse 1.4s ease-in-out ${i * 0.2}s infinite`,
                      }}
                    />
                  ))}
                </div>
              </div>
            </div>

            {/* 입력창 */}
            <div
              className="px-5 py-4"
              style={{ borderTop: "1px solid var(--border-subtle)" }}
            >
              <div
                className="flex items-center gap-3 px-4 py-3 rounded-2xl"
                style={{
                  backgroundColor: "rgba(255,255,255,0.04)",
                  border: "1px solid var(--border-subtle)",
                }}
              >
                <p className="text-sm flex-1" style={{ color: "var(--text-muted)" }}>
                  어떤 맥주를 찾고 계신가요?
                </p>
                <div
                  className="w-7 h-7 rounded-full flex items-center justify-center flex-shrink-0"
                  style={{ backgroundColor: "var(--amber)" }}
                >
                  <ChevronRight className="w-3.5 h-3.5 text-black" />
                </div>
              </div>

              {/* 빠른 선택 */}
              <div className="flex flex-wrap gap-2 mt-3">
                {["🍗 치킨", "🌞 여름날", "😴 혼자", "🎁 선물"].map((tag) => (
                  <span
                    key={tag}
                    className="text-xs px-3 py-1.5 rounded-full cursor-pointer transition-colors"
                    style={{
                      backgroundColor: "rgba(217,119,6,0.08)",
                      border: "1px solid var(--border-amber)",
                      color: "var(--text-secondary)",
                    }}
                  >
                    {tag}
                  </span>
                ))}
              </div>
            </div>
          </div>

          {/* 우측: 텍스트 */}
          <div>
            <p
              className="text-xs font-semibold tracking-[0.2em] uppercase mb-4"
              style={{ color: "var(--amber)" }}
            >
              AI 추천 엔진
            </p>
            <h2
              className="text-4xl md:text-5xl font-bold mb-6"
              style={{ fontFamily: "var(--font-playfair)", color: "var(--text-primary)" }}
            >
              말만 하세요,
              <br />
              <span className="text-gradient-amber">AI가 찾아드립니다</span>
            </h2>
            <p className="text-lg leading-relaxed mb-8" style={{ color: "var(--text-secondary)" }}>
              Claude AI가 자연어를 이해합니다. 복잡한 필터 없이 지금 상황을
              그냥 말하면, 맥락을 파악해 수백 종 중 최적의 맥주를 골라드립니다.
            </p>

            <div className="space-y-4">
              {advantages.map((adv, i) => (
                <div key={i} className="flex items-center gap-3">
                  <div
                    className="w-5 h-5 rounded-full flex items-center justify-center flex-shrink-0"
                    style={{ backgroundColor: "rgba(217,119,6,0.2)", border: "1px solid var(--border-amber)" }}
                  >
                    <Check className="w-3 h-3" style={{ color: "var(--amber)" }} />
                  </div>
                  <p className="text-sm" style={{ color: "var(--text-secondary)" }}>
                    {adv}
                  </p>
                </div>
              ))}
            </div>

            <div className="mt-10 flex flex-col sm:flex-row gap-4">
              <a
                href="#"
                className="btn-shimmer text-black font-semibold px-7 py-3.5 rounded-full text-sm flex items-center justify-center gap-2"
              >
                AI 추천 체험하기
                <Sparkles className="w-4 h-4" />
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════
   페어링 섹션
═══════════════════════════════════════════ */
function PairingSection() {
  const pairings = [
    {
      food: "🍗",
      foodName: "치킨",
      beers: ["IPA", "페일에일", "필스너"],
      description: "치킨의 기름기를 홉 쓴맛이 깔끔하게 잡아줍니다",
      color: "#f59e0b",
    },
    {
      food: "🥩",
      foodName: "삼겹살",
      beers: ["필스너", "라거", "세션 IPA"],
      description: "고기의 풍미를 라이트한 바디감이 부드럽게 감쌉니다",
      color: "#ef4444",
    },
    {
      food: "🍕",
      foodName: "피자",
      beers: ["앰버 에일", "페일에일", "위트"],
      description: "치즈의 짭조름함과 홉 과일향의 완벽한 하모니",
      color: "#f97316",
    },
    {
      food: "🍣",
      foodName: "초밥",
      beers: ["위트", "세션 라거", "사케 에일"],
      description: "깔끔한 탄산감이 생선의 선도를 더욱 살려줍니다",
      color: "#06b6d4",
    },
    {
      food: "🍫",
      foodName: "디저트",
      beers: ["초콜릿 스타우트", "포터", "임페리얼 스타우트"],
      description: "다크 초콜릿의 달콤함과 로스팅 향의 풍부한 앙상블",
      color: "#8b5cf6",
    },
    {
      food: "🧀",
      foodName: "치즈",
      beers: ["앰버 에일", "IPA", "듀엘"],
      description: "치즈의 깊은 풍미가 홉과 몰트의 복합미와 어우러집니다",
      color: "#eab308",
    },
  ];

  return (
    <section className="py-28 px-6">
      <div className="max-w-7xl mx-auto">
        <div className="text-center mb-16">
          <p
            className="text-xs font-semibold tracking-[0.2em] uppercase mb-4"
            style={{ color: "var(--amber)" }}
          >
            음식 페어링
          </p>
          <h2
            className="text-4xl md:text-5xl font-bold"
            style={{ fontFamily: "var(--font-playfair)", color: "var(--text-primary)" }}
          >
            먹는 것만큼 중요한
            <br />
            <span className="text-gradient-amber">마시는 것의 선택</span>
          </h2>
          <p className="mt-5 text-lg max-w-xl mx-auto" style={{ color: "var(--text-secondary)" }}>
            음식을 선택하면 최고의 맥주 궁합을 찾아드립니다
          </p>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-3 gap-5">
          {pairings.map((pair, i) => (
            <div
              key={i}
              className="card-hover rounded-2xl p-6 cursor-pointer"
              style={{
                backgroundColor: "var(--bg-card)",
                border: "1px solid var(--border-subtle)",
              }}
            >
              <div className="text-4xl mb-4">{pair.food}</div>
              <h3
                className="text-lg font-semibold mb-1"
                style={{ color: "var(--text-primary)" }}
              >
                {pair.foodName}
              </h3>
              <p className="text-xs mb-4 leading-relaxed" style={{ color: "var(--text-muted)" }}>
                {pair.description}
              </p>
              <div className="flex flex-wrap gap-1.5">
                {pair.beers.map((beer) => (
                  <span
                    key={beer}
                    className="text-xs px-2.5 py-1 rounded-full font-medium"
                    style={{
                      backgroundColor: pair.color + "18",
                      color: pair.color,
                      border: `1px solid ${pair.color}30`,
                    }}
                  >
                    {beer}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>

        {/* 날씨 연동 배너 */}
        <div
          className="mt-8 rounded-2xl p-6 flex flex-col sm:flex-row items-center gap-6"
          style={{
            background: "linear-gradient(135deg, rgba(217,119,6,0.1) 0%, rgba(180,83,9,0.05) 100%)",
            border: "1px solid var(--border-amber)",
          }}
        >
          <div
            className="w-14 h-14 rounded-2xl flex items-center justify-center flex-shrink-0"
            style={{ backgroundColor: "rgba(217,119,6,0.15)" }}
          >
            <CloudSun className="w-7 h-7" style={{ color: "var(--amber-glow)" }} />
          </div>
          <div className="flex-1 text-center sm:text-left">
            <h3 className="font-semibold mb-1" style={{ color: "var(--text-primary)" }}>
              날씨 연동 자동 추천
            </h3>
            <p className="text-sm" style={{ color: "var(--text-secondary)" }}>
              지금 날씨를 분석해 이 순간 가장 어울리는 맥주를 자동으로 제안합니다.
              오늘 기온 28°C — 상쾌한 세션 IPA가 딱입니다.
            </p>
          </div>
          <a
            href="#"
            className="btn-shimmer text-black text-sm font-semibold px-6 py-3 rounded-full flex-shrink-0 flex items-center gap-2"
          >
            오늘의 추천 보기
            <ArrowRight className="w-4 h-4" />
          </a>
        </div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════
   리뷰 섹션
═══════════════════════════════════════════ */
function ReviewsSection() {
  const reviews = [
    {
      name: "김지수",
      handle: "@jisu_brews",
      avatar: "👩",
      text: "맥주에 대해 아무것도 몰랐는데 온보딩 3분 만에 내 취향을 정확히 파악해줬어요. 추천받은 IPA 마셨다가 완전히 빠졌습니다 😍",
      rating: 5,
      beers: 34,
    },
    {
      name: "박현우",
      handle: "@craft_hyunwoo",
      avatar: "🧔",
      text: "삼겹살 집에서 '지금 먹고 있는 음식에 어울리는 맥주 추천해줘' 했더니 바로 근처 편의점에서 살 수 있는 맥주까지 알려주더라고요. 실용적이에요.",
      rating: 5,
      beers: 127,
    },
    {
      name: "이서연",
      handle: "@seo_beer_diary",
      avatar: "👱",
      text: "크래프트 맥주 마니아로서 희귀 맥주 정보도 꼼꼼하게 나와 있어서 만족스럽습니다. 맥주 일기 기능이 특히 좋아요.",
      rating: 5,
      beers: 263,
    },
    {
      name: "최민준",
      handle: "@minjun_hops",
      avatar: "🧑",
      text: "선물용으로 맥주 세트 고를 때 늘 고민이었는데, '맥주 좋아하는 친구 선물' 검색하니까 취향별 세트 큐레이션이 딱 나왔어요. 대박",
      rating: 5,
      beers: 58,
    },
    {
      name: "정혜진",
      handle: "@hjbrewlover",
      avatar: "👩",
      text: "연말 음주 리포트가 스포티파이 위에트 느낌이라 너무 좋았어요. 올해 내가 마신 맥주 스타일이 한눈에 보이니까 뿌듯하달까요 ㅋㅋ",
      rating: 5,
      beers: 89,
    },
    {
      name: "강도윤",
      handle: "@doyun_beer",
      avatar: "🧔",
      text: "날씨 연동 추천이 진짜 신기해요. 비 오는 날엔 자동으로 스타우트나 포터를 추천해주는데, 마셔보면 진짜 딱 맞아요.",
      rating: 5,
      beers: 41,
    },
  ];

  return (
    <section
      className="py-28 px-6"
      style={{ backgroundColor: "var(--bg-surface)" }}
    >
      <div className="max-w-7xl mx-auto">
        <div className="text-center mb-16">
          <p
            className="text-xs font-semibold tracking-[0.2em] uppercase mb-4"
            style={{ color: "var(--amber)" }}
          >
            사용자 리뷰
          </p>
          <h2
            className="text-4xl md:text-5xl font-bold"
            style={{ fontFamily: "var(--font-playfair)", color: "var(--text-primary)" }}
          >
            이미 수천 명이
            <br />
            <span className="text-gradient-amber">완벽한 맥주를 찾았습니다</span>
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {reviews.map((review, i) => (
            <div
              key={i}
              className="card-hover rounded-2xl p-6"
              style={{
                backgroundColor: "var(--bg-card)",
                border: "1px solid var(--border-subtle)",
              }}
            >
              <div className="flex items-center gap-1 mb-4">
                {[...Array(review.rating)].map((_, j) => (
                  <Star key={j} className="w-4 h-4 fill-amber-400 text-amber-400" />
                ))}
              </div>

              <div className="relative mb-5">
                <Quote
                  className="absolute -top-1 -left-1 w-5 h-5 opacity-30"
                  style={{ color: "var(--amber)" }}
                />
                <p className="text-sm leading-relaxed pl-5" style={{ color: "var(--text-secondary)" }}>
                  {review.text}
                </p>
              </div>

              <div className="flex items-center justify-between pt-4" style={{ borderTop: "1px solid var(--border-subtle)" }}>
                <div className="flex items-center gap-3">
                  <div
                    className="w-9 h-9 rounded-full flex items-center justify-center text-lg"
                    style={{ backgroundColor: "var(--bg-surface)" }}
                  >
                    {review.avatar}
                  </div>
                  <div>
                    <p className="text-sm font-semibold" style={{ color: "var(--text-primary)" }}>
                      {review.name}
                    </p>
                    <p className="text-xs" style={{ color: "var(--text-muted)" }}>
                      {review.handle}
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-xs" style={{ color: "var(--text-muted)" }}>
                    기록한 맥주
                  </p>
                  <p className="text-sm font-bold" style={{ color: "var(--amber)" }}>
                    {review.beers}잔
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* 신뢰 지표 */}
        <div className="grid grid-cols-3 gap-5 mt-10">
          {[
            { icon: BarChart3, value: "NPS 67", label: "순추천고객지수" },
            { icon: Star, value: "4.9/5", label: "앱스토어 평점" },
            { icon: Gift, value: "월 1,200건", label: "구독 박스 판매" },
          ].map((metric, i) => {
            const Icon = metric.icon;
            return (
              <div
                key={i}
                className="text-center py-6 rounded-2xl"
                style={{
                  backgroundColor: "var(--bg-card)",
                  border: "1px solid var(--border-subtle)",
                }}
              >
                <Icon className="w-5 h-5 mx-auto mb-3" style={{ color: "var(--amber)" }} />
                <p
                  className="text-2xl font-bold"
                  style={{
                    fontFamily: "var(--font-playfair)",
                    color: "var(--amber-glow)",
                  }}
                >
                  {metric.value}
                </p>
                <p className="text-xs mt-1" style={{ color: "var(--text-muted)" }}>
                  {metric.label}
                </p>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════
   CTA 섹션
═══════════════════════════════════════════ */
function CTASection() {
  return (
    <section className="py-28 px-6 relative overflow-hidden">
      {/* 앰버 글로우 */}
      <div
        className="absolute inset-0 pointer-events-none"
        style={{
          background: "radial-gradient(ellipse 80% 60% at 50% 50%, rgba(217,119,6,0.12) 0%, transparent 70%)",
        }}
      />

      <div className="max-w-3xl mx-auto relative text-center">
        <div
          className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-semibold mb-8"
          style={{
            background: "rgba(217,119,6,0.12)",
            border: "1px solid var(--border-amber)",
            color: "var(--amber-glow)",
            letterSpacing: "0.08em",
          }}
        >
          <Beer className="w-3 h-3" />
          지금 바로 시작하세요
        </div>

        <h2
          className="text-5xl md:text-6xl font-bold mb-6"
          style={{
            fontFamily: "var(--font-playfair)",
            color: "var(--text-primary)",
            lineHeight: 1.1,
          }}
        >
          당신의 첫 번째
          <br />
          <span className="text-gradient-amber">완벽한 맥주</span>를
          <br />
          찾아드릴게요
        </h2>

        <p className="text-lg mb-10 max-w-xl mx-auto" style={{ color: "var(--text-secondary)" }}>
          3분 취향 설문만 완료하면 바로 맞춤 추천을 받을 수 있어요.
          신용카드 불필요, 무료로 시작하세요.
        </p>

        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <a
            href="#"
            className="btn-shimmer text-black font-bold px-10 py-4 rounded-full text-lg flex items-center justify-center gap-2"
          >
            3분 취향 설문 시작
            <ArrowRight className="w-5 h-5" />
          </a>
          <a
            href="#"
            className="btn-outline-amber px-10 py-4 rounded-full text-lg font-medium flex items-center justify-center gap-2"
          >
            맥주 탐색하기
            <Beer className="w-5 h-5" />
          </a>
        </div>

        <p className="mt-6 text-sm" style={{ color: "var(--text-muted)" }}>
          500+ 크래프트 맥주 DB · AI 기반 개인화 추천 · 무료 플랜 제공
        </p>
      </div>
    </section>
  );
}

/* ═══════════════════════════════════════════
   푸터
═══════════════════════════════════════════ */
function Footer() {
  return (
    <footer
      style={{
        borderTop: "1px solid var(--border-subtle)",
        backgroundColor: "var(--bg-surface)",
      }}
    >
      <div className="max-w-7xl mx-auto px-6 py-12">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8 mb-10">
          {/* 브랜드 */}
          <div className="col-span-2 md:col-span-1">
            <div className="flex items-center gap-2.5 mb-4">
              <div
                style={{ backgroundColor: "var(--amber)" }}
                className="w-8 h-8 rounded-lg flex items-center justify-center"
              >
                <Beer className="w-4.5 h-4.5 text-black" strokeWidth={2.5} />
              </div>
              <span
                style={{ fontFamily: "var(--font-playfair)", color: "var(--text-primary)" }}
                className="text-xl font-bold"
              >
                BrewPick
              </span>
            </div>
            <p className="text-sm leading-relaxed" style={{ color: "var(--text-muted)" }}>
              AI가 큐레이션하는<br />나만의 맥주 탐험
            </p>
          </div>

          {/* 링크들 */}
          {[
            {
              title: "서비스",
              links: ["AI 추천", "맥주 탐색", "페어링 가이드", "컬렉션"],
            },
            {
              title: "커뮤니티",
              links: ["리뷰 작성", "큐레이터 팔로우", "이달의 맥주", "이벤트"],
            },
            {
              title: "회사",
              links: ["소개", "블로그", "채용", "문의"],
            },
          ].map((col) => (
            <div key={col.title}>
              <h4
                className="text-sm font-semibold mb-4"
                style={{ color: "var(--text-secondary)" }}
              >
                {col.title}
              </h4>
              <ul className="space-y-2.5">
                {col.links.map((link) => (
                  <li key={link}>
                    <a
                      href="#"
                      className="text-sm transition-colors"
                      style={{ color: "var(--text-muted)" }}
                    >
                      {link}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div
          className="flex flex-col sm:flex-row items-center justify-between pt-8 gap-4"
          style={{ borderTop: "1px solid var(--border-subtle)" }}
        >
          <p className="text-xs" style={{ color: "var(--text-muted)" }}>
            © 2026 BrewPick. 만 19세 이상만 이용 가능합니다.
          </p>
          <p className="text-xs" style={{ color: "var(--text-muted)" }}>
            개인정보처리방침 · 이용약관 · 사업자정보
          </p>
        </div>
      </div>
    </footer>
  );
}
