# 🎬 Movie App - Modern Android Development

> Jetpack Compose 기반 영화 정보 앱 (TMDB API 연동)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-2025.10.01-green.svg)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/Hilt-2.59.2-orange.svg)](https://dagger.dev/hilt/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-brightgreen.svg)](https://developer.android.com/studio/releases/platforms)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-brightgreen.svg)](https://developer.android.com/studio/releases/platforms)

## 📱 프로젝트 소개

최신 Android 개발 트렌드를 학습하기 위한 토이 프로젝트입니다. TMDB API를 활용하여 영화 목록 조회, 검색, 상세 정보 확인 기능을 구현했으며, **Jetpack Compose**와 **Clean Architecture 원칙**을 적용했습니다.

### 🎯 학습 목표
- ✅ Jetpack Compose 기반 선언형 UI 개발
- ✅ MVVM 아키텍처 패턴 적용
- ✅ Hilt를 통한 의존성 주입
- ✅ Coroutines & Flow를 활용한 비동기 처리
- ✅ DTO ↔ Domain Model 분리를 통한 Clean Architecture 실습
- ✅ Unit Test 작성 및 테스트 가능한 코드 설계

---

## 🎥 주요 기능

### 1️⃣ 영화 목록
- 인기 영화 목록 조회 (TMDB API)
- 실시간 검색 (Debounce 300ms)
- LazyColumn 무한 스크롤 (Pagination)

### 2️⃣ 영화 상세
- 포스터, 제목, 평점, 줄거리 표시
- 이미지 캐싱 (Coil 3.x)

### 3️⃣ WebView 연동
- JavaScript Bridge 구현
- 웹에서 네이티브 Toast 호출

---

## 🛠️ 기술 스택

### **UI Layer**
- **Jetpack Compose** - 선언형 UI
- **Material 3** - 최신 디자인 시스템
- **Navigation Compose** - 화면 전환
- **Coil 3.x** - 이미지 로딩 및 캐싱

### **Architecture**
- **MVVM** - 아키텍처 패턴
- **Repository Pattern** - 데이터 접근 추상화
- **DTO → Domain Model** - 레이어 분리

### **Dependency Injection**
- **Hilt** - 의존성 주입
- **Dagger** - 컴파일 타임 DI

### **Asynchronous**
- **Kotlin Coroutines** - 비동기 처리
- **Flow & StateFlow** - 반응형 데이터 스트림

### **Network**
- **Retrofit** - REST API 클라이언트
- **Gson** - JSON 직렬화

### **Testing**
- **JUnit 5** - 단위 테스트
- **MockK** - Mocking 라이브러리
- **Turbine** - Flow 테스트
- **Truth** - Assertion 라이브러리

---

## 🏗️ 아키텍처

### **MVVM + Repository Pattern**

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌──────────────┐         ┌──────────────┐                 │
│  │   Screen     │ ──────> │  ViewModel   │                 │
│  │  (Compose)   │ <────── │  (StateFlow) │                 │
│  └──────────────┘         └──────────────┘                 │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼ (interface)
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Movie (Domain Model)                                │   │
│  │  - Non-null                                          │   │
│  │  - Business logic focused                            │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼ (implementation)
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
│  ┌──────────────────┐       ┌──────────────────────────┐    │
│  │  Repository      │ ────> │  MovieApi (Retrofit)     │    │
│  │  (Hilt)          │ <──── │  MovieResponse (DTO)     │    │
│  └──────────────────┘       └──────────────────────────┘    │
│           │                                                   │
│           └─> toDomain() : Movie?  (DTO → Domain 변환)      │
└─────────────────────────────────────────────────────────────┘
```

### **핵심 설계 원칙**

#### 1. DTO ↔ Domain Model 분리
```kotlin
// DTO (Data Layer) - API 의존적
data class MovieResponse(
    @SerializedName("poster_path") val posterPath: String?,  // nullable
    @SerializedName("vote_average") val voteAverage: Double?
)

// Domain Model - 비즈니스 로직 중심
data class Movie(
    val posterImageUrl: String,  // non-null, 전체 URL
    val rating: Double           // 0.0~10.0 보장
)

// Mapping - null 처리 및 데이터 가공
fun MovieResponse.toDomain(): Movie? {
    if (id == null || title.isNullOrBlank()) return null

    return Movie(
        posterImageUrl = "https://image.tmdb.org/t/p/w500$posterPath",
        rating = (voteAverage ?: 0.0).coerceIn(0.0, 10.0)
    )
}
```

#### 2. 단방향 데이터 플로우
```kotlin
@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieListUiState>(Loading)
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    // UI → ViewModel (Event)
    fun onQueryChange(query: String) {
        _query.value = query
    }

    // ViewModel → UI (State)
    val uiState = combine(_movies, _query, _isLoading, _error) { ... }
        .stateIn(viewModelScope, ...)
}
```

#### 3. Hilt DI
```kotlin
@HiltAndroidApp
class MovieApplication : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity()

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel()

@Singleton
class MovieRepository @Inject constructor() { ... }
```

---

## 📂 프로젝트 구조

```
app/src/main/java/com/example/test_code/
├── 📁 presentation/          # UI Layer
│   ├── list/
│   │   ├── MovieListScreen.kt       # Compose UI
│   │   ├── MovieListViewModel.kt    # ViewModel
│   │   └── MovieListUiState.kt      # UI State
│   ├── detail/
│   │   ├── MovieDetailScreen.kt
│   │   ├── MovieDetailViewModel.kt
│   │   └── MovieDetailUiState.kt
│   ├── Navigation.kt                # Navigation Graph
│   └── WebViewScreen.kt             # WebView + JS Bridge
│
├── 📁 domain/                # Domain Layer
│   ├── model/
│   │   └── Movie.kt                 # Domain Model (Non-null)
│   └── repository/                  # (Optional for Clean Architecture)
│
├── 📁 data/                  # Data Layer
│   ├── repository/
│   │   └── MovieRepository.kt       # Repository Implementation
│   └── remote/
│       ├── MovieApi.kt              # Retrofit API
│       ├── MovieResponse.kt         # DTO (Nullable)
│       └── RetrofitClient.kt
│
├── 📁 di/                    # Dependency Injection
│   └── (Hilt Modules)
│
└── MovieApplication.kt       # Application Class
```

---

## 🚀 시작하기

### 필수 조건
- Android Studio Ladybug (2024.2.1+)
- JDK 17
- Gradle 9.2.1+
- Min SDK 24 (Android 7.0)

### 설치 및 실행

1. **Clone Repository**
   ```bash
   git clone https://github.com/your-username/movie-app.git
   cd movie-app
   ```

2. **API Key 설정**

   [TMDB API](https://www.themoviedb.org/settings/api)에서 API Key를 발급받은 후:

   `local.properties` 파일에 추가:
   ```properties
   tmdb.api.key=YOUR_API_KEY_HERE
   ```

3. **빌드 및 실행**
   ```bash
   ./gradlew assembleDebug
   ```

   또는 Android Studio에서 Run ▶️

---

## 🧪 테스트

### Unit Test 실행
```bash
./gradlew test
```

### Test Coverage
```bash
./gradlew testDebugUnitTest jacocoTestReport
```

### 주요 테스트 케이스
- ✅ MovieListViewModel - 영화 목록 로딩 성공/실패
- ✅ MovieListViewModel - 검색어 입력 시 필터링
- ✅ Repository - DTO → Domain 변환 (null 처리)

---

## 📸 스크린샷

| 영화 목록 | 영화 상세 | 검색 |
|-----------|-----------|------|
| ![List](docs/screenshots/list.png) | ![Detail](docs/screenshots/detail.png) | ![Search](docs/screenshots/search.png) |

> 📌 스크린샷은 `docs/screenshots/` 폴더에 추가 예정

---

## 💡 주요 학습 내용

### 1. Jetpack Compose
- `LazyColumn`으로 효율적인 리스트 렌더링
- `remember`, `collectAsState`로 상태 관리
- `derivedStateOf`로 리컴포지션 최적화
- Material 3 컴포넌트 활용

### 2. Coroutines & Flow
- `viewModelScope`로 생명주기 인지 코루틴
- `StateFlow`로 상태 스트림 관리
- `combine`으로 여러 Flow 결합
- `debounce`로 검색 입력 최적화

### 3. Clean Architecture
- DTO와 Domain Model 분리
- Repository Pattern으로 데이터 접근 추상화
- 레이어별 책임 분리 (Presentation ↔ Domain ↔ Data)
- `mapNotNull`로 유효하지 않은 데이터 필터링

### 4. Dependency Injection
- `@HiltAndroidApp`, `@AndroidEntryPoint` 이해
- `@HiltViewModel`로 ViewModel 주입
- `@Inject constructor`로 자동 의존성 주입
- 컴파일 타임 코드 생성 원리 학습

### 5. Testing
- JUnit 5로 단위 테스트 작성
- MockK로 Repository Mocking
- Turbine으로 Flow 테스트
- Given-When-Then 패턴 적용

---

## 🔄 개선 예정 사항

### 단기 (1-2주)
- [ ] Type-safe Navigation 적용 (Kotlin Serialization)
- [ ] Paging 3 라이브러리 도입 (무한 스크롤 최적화)
- [ ] Room Database 추가 (찜하기 기능)
- [ ] Pull to Refresh 구현

### 중기 (1개월)
- [ ] UI Test 추가 (Compose Test)
- [ ] GitHub Actions CI/CD 구축
- [ ] Detekt 린트 규칙 추가
- [ ] Code Coverage 80% 달성

### 장기 (관심 분야)
- [ ] Compose Multiplatform 학습 (iOS 지원)
- [ ] MVI 아키텍처 적용 실험
- [ ] Kotlin Multiplatform 탐구

---

## 📚 참고 자료

### 공식 문서
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Hilt Guide](https://dagger.dev/hilt/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [TMDB API](https://developers.themoviedb.org/3)

### 학습 자료
- [Now in Android (Google Sample)](https://github.com/android/nowinandroid)
- [Compose Samples](https://github.com/android/compose-samples)
- [Clean Architecture (Uncle Bob)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

## 🤝 기여

이슈 및 PR은 언제나 환영합니다! 🙏

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 라이선스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

---

## 👤 개발자

**Dongkyu**

- GitHub: [@your-github](https://github.com/your-username)
- Blog: [your-blog.com](https://your-blog.com)
- Email: your.email@example.com

---

## 🙏 Acknowledgments

- [TMDB](https://www.themoviedb.org/) - 영화 데이터 제공
- [Google Codelabs](https://developer.android.com/courses) - Compose 학습 자료
- [Android Developers](https://www.youtube.com/@AndroidDevelopers) - YouTube 강의

---

<div align="center">

**⭐ Star this repository if you find it helpful!**

Made with ❤️ and Kotlin

</div>
