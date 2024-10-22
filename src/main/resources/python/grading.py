# -*- coding: utf-8 -*-
from __future__ import division     #Python 3.x의 나눗셈 스타일 사용
from collections import Counter

# 유니코드로 상수 정의
CHOSUNG = u"ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
JUNGSUNG = u"ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"
JONGSUNG = u"ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ"

def decompose_korean(text):
    # 입력을 유니코드로 변환
    if not isinstance(text, unicode):
        text = text.decode('utf-8')

    result = []
    for char in text:
        if u'가' <= char <= u'힣':  # 한글 범위
            char_code = ord(char) - ord(u'가')
            cho = char_code // (21 * 28)
            jung = (char_code % (21 * 28)) // 28
            jung = 1 if jung == 5 else jung
            jong = char_code % 28

            result.append(CHOSUNG[cho])
            result.append(JUNGSUNG[jung])
            if jong > 0:
                result.append(JONGSUNG[jong - 1])
        else:
            result.append(char)
    return u''.join(result)

def is_transliteration(word):
    if not isinstance(word, unicode):
        word = word.decode('utf-8')

    common_patterns = [
        u"션", u"터", u"티", u"디", u"브", u"크", u"지", u"매니",
        u"링", u"팅", u"킹", u"밍", u"닝", u"슨", u"쳐", u"플",
        u"프", u"스", u"트", u"드", u"그", u"젝", u"워", u"셔",
        u"즈", u"츠", u"버", u"퓨", u"뷰", u"큐", u"듀", u"튜",
        u"씨", u"폰", u"컨", u"던", u"텐", u"맨", u"잉", u"벤",
        u"익", u"릭", u"닉", u"픽", u"믹", u"윙", u"빙", u"밴",
        u"케", u"테"
    ]

    if any(p in word for p in common_patterns):
        return True

    if word and word[0] in u"ㄲㄸㅃㅆㅉ":
        return True

    vowel_combinations = [u"ㅐㅣ", u"ㅔㅣ", u"ㅏㅣ", u"ㅓㅣ", u"ㅗㅣ", u"ㅜㅣ"]
    decomposed = decompose_korean(word)
    for i in range(len(decomposed) - 1):
        if decomposed[i] + decomposed[i+1] in vowel_combinations:
            return True

    return False

def levenshtein_distance(s1, s2):
    if len(s1) < len(s2):
        return levenshtein_distance(s2, s1)
    if len(s2) == 0:
        return len(s1)
    previous_row = range(len(s2) + 1)
    for i, c1 in enumerate(s1):
        current_row = [i + 1]
        for j, c2 in enumerate(s2):
            insertions = previous_row[j + 1] + 1
            deletions = current_row[j] + 1
            substitutions = previous_row[j] + (c1 != c2)
            current_row.append(min(insertions, deletions, substitutions))
        previous_row = current_row
    return previous_row[-1]

def levenshtein_similarity(s1, s2):
    if not isinstance(s1, unicode):
        s1 = s1.decode('utf-8')
    if not isinstance(s2, unicode):
        s2 = s2.decode('utf-8')
    distance = levenshtein_distance(s1, s2)
    max_length = max(len(s1), len(s2))
    return 1 - (distance / max_length)

def jaro_winkler_similarity(s1, s2, scaling_factor=0.1):
    if not isinstance(s1, unicode):
        s1 = s1.decode('utf-8')
    if not isinstance(s2, unicode):
        s2 = s2.decode('utf-8')

    def jaro_distance(s1, s2):
        if len(s1) == 0 and len(s2) == 0:
            return 1.0
        if len(s1) == 0 or len(s2) == 0:
            return 0.0

        match_distance = (max(len(s1), len(s2)) // 2) - 1
        s1_matches = [0] * len(s1)
        s2_matches = [0] * len(s2)
        matches = 0
        transpositions = 0

        for i in range(len(s1)):
            start = max(0, i - match_distance)
            end = min(i + match_distance + 1, len(s2))
            for j in range(start, end):
                if s2_matches[j]:
                    continue
                if s1[i] != s2[j]:
                    continue
                s1_matches[i] = 1
                s2_matches[j] = 1
                matches += 1
                break

        if matches == 0:
            return 0.0

        k = 0
        for i in range(len(s1)):
            if not s1_matches[i]:
                continue
            while k < len(s2) and not s2_matches[k]:
                k += 1
            if k < len(s2) and s1[i] != s2[k]:
                transpositions += 1
            k += 1

        return ((matches / len(s1)) +
                (matches / len(s2)) +
                ((matches - transpositions / 2.0) / matches)) / 3.0

    jaro_dist = jaro_distance(s1, s2)
    prefix_length = 0
    for i in range(min(len(s1), len(s2), 4)):
        if s1[i] == s2[i]:
            prefix_length += 1
        else:
            break
    return jaro_dist + (scaling_factor * prefix_length * (1 - jaro_dist))

def improved_jaro_winkler_similarity(word1, word2):
    if not isinstance(word1, unicode):
        word1 = word1.decode('utf-8')
    if not isinstance(word2, unicode):
        word2 = word2.decode('utf-8')

    original_sim = jaro_winkler_similarity(word1.lower(), word2.lower())
    decomposed_sim = jaro_winkler_similarity(decompose_korean(word1.lower()),
                                             decompose_korean(word2.lower()))
    return 0.6 * original_sim + 0.4 * decomposed_sim

def ngram_similarity(s1, s2, n=2):
    def get_ngrams(s, n):
        return [u''.join(gram) for gram in zip(*[s[i:] for i in range(n)])]

    if not isinstance(s1, unicode):
        s1 = s1.decode('utf-8')
    if not isinstance(s2, unicode):
        s2 = s2.decode('utf-8')

    s1_ngrams = Counter(get_ngrams(s1, n))
    s2_ngrams = Counter(get_ngrams(s2, n))

    intersection = sum((s1_ngrams & s2_ngrams).values())
    union = sum((s1_ngrams | s2_ngrams).values())

    return intersection / union if union != 0 else 0

def cosine_similarity(s1, s2):
    if not isinstance(s1, unicode):
        s1 = s1.decode('utf-8')
    if not isinstance(s2, unicode):
        s2 = s2.decode('utf-8')

    vec1 = Counter(s1)
    vec2 = Counter(s2)

    intersection = set(vec1.keys()) & set(vec2.keys())

    numerator = sum([vec1[x] * vec2[x] for x in intersection])
    sum1 = sum([vec1[x]**2 for x in vec1.keys()])
    sum2 = sum([vec2[x]**2 for x in vec2.keys()])
    denominator = (sum1 * sum2)**0.5

    return numerator / denominator if denominator else 0.0

def lcs_length(s1, s2):
    if not isinstance(s1, unicode):
        s1 = s1.decode('utf-8')
    if not isinstance(s2, unicode):
        s2 = s2.decode('utf-8')

    m, n = len(s1), len(s2)
    L = [[0] * (n + 1) for _ in range(m + 1)]
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if s1[i-1] == s2[j-1]:
                L[i][j] = L[i-1][j-1] + 1
            else:
                L[i][j] = max(L[i-1][j], L[i][j-1])
    return L[m][n]

def lcs_similarity(s1, s2):
    if not isinstance(s1, unicode):
        s1 = s1.decode('utf-8')
    if not isinstance(s2, unicode):
        s2 = s2.decode('utf-8')

    lcs_len = lcs_length(s1, s2)
    return lcs_len / max(len(s1), len(s2))

def comprehensive_grade(word1, word2, weights):
    # 입력값 유니코드 변환
    if not isinstance(word1, unicode):
        word1 = word1.decode('utf-8')
    if not isinstance(word2, unicode):
        word2 = word2.decode('utf-8')

    # weights를 리스트로 변환
    weights = list(weights)

    word1 = word1.lower()
    word2 = word2.lower()

    decomposed1 = decompose_korean(word1)
    decomposed2 = decompose_korean(word2)

    similarities = [
        levenshtein_similarity(decomposed1, decomposed2),
        improved_jaro_winkler_similarity(word1, word2),
        ngram_similarity(decomposed1, decomposed2, n=2),
        cosine_similarity(decomposed1, decomposed2),
        lcs_similarity(decomposed1, decomposed2)
    ]

    return sum(w * s for w, s in zip(weights, similarities))