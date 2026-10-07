package com.example.data.engine

import com.example.data.model.*

object DeterministicGameEngine {

    fun processTurn(
        playerAction: String,
        actionType: ActionType?,
        currentVitals: VitalStats,
        currentSocial: SocialMeters,
        copperCoins: Int,
        manaComprehension: Int,
        inventory: List<InventoryItem>,
        turnCount: Int
    ): GameEngineTurnResponse {
        val lowerText = playerAction.trim().lowercase()

        // 1. Initial Encounter with Roger (Turn 1 / 2)
        if (turnCount <= 2 && (lowerText.contains("روجر") || actionType != null || lowerText.contains("بكاء") || lowerText.contains("مفاوض") || lowerText.contains("هرب") || lowerText.contains("حجر"))) {
            return when (actionType) {
                ActionType.CHILD_FACADE -> {
                    GameEngineTurnResponse(
                        narrativeAr = "تخفض كتفيك وتجهش بالبكاء متصنعاً الرعب الطفولي مع نوبة سعال جافة متقنة. جسدك النحيل يرتعش في الصقيع، مما يجعل منظر طفل مريض ومعدٍ مثيراً للاشمئزاز بالنسبة لبلطجي قاسٍ يخشى أمراض الأزقة الموبوءة.",
                        npcDialogue = NpcDialogue(
                            speakerName = "روجر الأعور",
                            speechAr = "تباً لك ولضعفك المقزز! ابتعد عني ولا تسعل في وجهي! اسمع جيداً يا ليو: أمامك حتى غروب شمس اليوم... إن لم تجلب عشر عملات كاملة فسأبيعك حياً لعمال مناجم الملح!",
                            tone = NpcTone.MOCKING
                        ),
                        energyDelta = -5,
                        hungerDelta = 5,
                        warmthDelta = -3,
                        suspicionDelta = -5,
                        childMaskDelta = -10,
                        copperDelta = 0,
                        manaInsightDelta = 0,
                        reputationDelta = 2,
                        suggestedActions = listOf(
                            SuggestedAction("التسلل إلى سوق الحبوب بحثاً عن بقايا طعام أو عمل", ActionType.DARING, "استعادة الطاقة وخفض الجوع"),
                            SuggestedAction("التوجه نحو دكان الساحر مورفاث لمراقبة نقوش المانا", ActionType.SCIENTIFIC, "دراسة الفيزياء السحرية للرونيات"),
                            SuggestedAction("البحث عن متسولين آخرين لجمع معلومات عن التجار", ActionType.STRATEGIC, "بناء شبكة استخبارات الأزقة")
                        )
                    )
                }
                ActionType.STRATEGIC -> {
                    GameEngineTurnResponse(
                        narrativeAr = "تقف بهدوء غير طبيعي لطفل يتجمد، وتثبت عينيك في عينه السليمة بنظرة باردة يندر أن يراها في شوارع إلدوريا. تتحدث بنبرة مستشار استراتيجي يعرض أرباحاً مدروسة: 'الخمس عملات لا تساوي شيئاً يا روجر. لقد راقبت تاجر النبيذ يخفي ثلاثة براميل مسروقة في المستودع الخلفي. أعطني فرصة وسأجعلك تبتزه بخمسين عملة الليلة.'",
                        npcDialogue = NpcDialogue(
                            speakerName = "روجر الأعور",
                            speechAr = "ما هذا الكلام؟ منذ متى يتحدث صبي الشوارع مثلك كتاجر مخضرم في ديوان الضرائب؟! هل أنت ممسوس بشيطان أم أرسلك أحدهم للتجسس علي؟... لكن انتظر، خمسون عملة؟ تكلم فوراً قبل أن أهشم رأسك!",
                            tone = NpcTone.SHOCKED
                        ),
                        energyDelta = -3,
                        hungerDelta = 3,
                        warmthDelta = -2,
                        suspicionDelta = 22, // High suspicion from adult speech
                        childMaskDelta = -25,
                        copperDelta = 2, // Roger throws 2 coins to grease the info
                        manaInsightDelta = 5,
                        reputationDelta = 10,
                        itemsAdded = listOf(
                            InventoryItem(
                                itemId = "merchant_note",
                                nameAr = "ملاحظة موقع براميل النبيذ",
                                descAr = "خريطة ذهنية كتبتها بالطبشور لثغرة حراسة التاجر",
                                utilityAr = "ابتزاز أو مساومة التاجر"
                            )
                        ),
                        suggestedActions = listOf(
                            SuggestedAction("التراجع بذكاء وادعاء أنك سمعت الفرسان يقولون ذلك (خفض الشك)", ActionType.CHILD_FACADE, "حماية نفسك من تهمة الاستحواذ"),
                            SuggestedAction("مواصلة التفاوض البارد وفرض شروط شراكة بنسبة 30%", ActionType.STRATEGIC, "السيطرة على روجر مالياً"),
                            SuggestedAction("استخدام الملاحظة كطُعم للفرار نحو حي الأكاديمية", ActionType.DARING, "الابتعاد عن رقابة العصابة")
                        )
                    )
                }
                ActionType.DARING -> {
                    GameEngineTurnResponse(
                        narrativeAr = "تستغل توازن روجر غير المستقر على الجليد، فتلقي بحصاة الصوان نحو كومة براميل متهالكة لتحدث صوتاً مدوياً وتصرخ: 'دورية فرسان المعبد قادمون!' بينما يلتفت مذعوراً، تركل الطين المتجمد تحت كعبه فينزلق بجسده الضخم في الوحل، وتفر كالسهم عبر الأزقة الضيقة.",
                        npcDialogue = NpcDialogue(
                            speakerName = "روجر الأعور",
                            speechAr = "أيها الفأر الصغير اللعين! سأمسك بك وأسلخ جلدك حياً عندما أراك مجدداً!",
                            tone = NpcTone.THREATENING
                        ),
                        energyDelta = -12,
                        hungerDelta = 8,
                        warmthDelta = 8, // Running generates warmth
                        suspicionDelta = 5,
                        childMaskDelta = 5,
                        copperDelta = 0,
                        manaInsightDelta = 8,
                        reputationDelta = 5,
                        suggestedActions = listOf(
                            SuggestedAction("الاختباء خلف إسطبلات الخيول الدافئة وسرقة جزر", ActionType.DARING, "استعادة الدفء وسد الجوع"),
                            SuggestedAction("فحص حجر الصوان والطبشور لدراسة تفاعلات الاحتكاك", ActionType.SCIENTIFIC, "توليد نار بفيزياء نقية"),
                            SuggestedAction("الاندماج في حشود المتسولين أمام البوابة الكبرى", ActionType.CHILD_FACADE, "تجنب ملاحقة روجر")
                        )
                    )
                }
                else -> {
                    // Default / Scientific action
                    GameEngineTurnResponse(
                        narrativeAr = "تخرج قطعة الطبشور وحجر الصوان بهدوء، وترسم بسرعة خطاً بزاوية 45 درجة على البرميل الخشبي المتجمد لحساب زاوية الارتداد الميكانيكي. عندما يرفع روجر عصاه، تضرب الحجر في شق الشحم المجمد فتحدث شرارة تصيب سترته الرثة برائحة شياط!",
                        npcDialogue = NpcDialogue(
                            speakerName = "روجر الأعور",
                            speechAr = "نار؟! كيف أشعلتها دون تعويذة كاهن النور؟! من أين تعلمت هذا السحر الشيطاني يا لقيط؟!",
                            tone = NpcTone.SHOCKED
                        ),
                        energyDelta = -8,
                        hungerDelta = 5,
                        warmthDelta = 12,
                        suspicionDelta = 18,
                        childMaskDelta = -15,
                        copperDelta = 0,
                        manaInsightDelta = 15,
                        reputationDelta = 8,
                        newTheorem = DiscoveredTheorem(
                            id = "th_thermo_ignition",
                            titleAr = "معادلة الاحتكاك والاشتعال الكهروستاتيكي",
                            physicsLawAr = "التحول الحراري للطاقة الحركية عبر تركيز الإجهاد السطحي",
                            manaApplicationAr = "إثارة مانا النار المحيطة بالاحتكاك الدقيق دون الحاجة لتراتيل المعبد الكنسية المعقدة!",
                            discoveryTurn = turnCount
                        ),
                        suggestedActions = listOf(
                            SuggestedAction("استغلال ذهوله للركض نحو سوق المدينة الصاخب", ActionType.DARING, "الهروب في الزحام"),
                            SuggestedAction("إيهامه بأنك تملك المزيد من الشرارات لحماية نفسك", ActionType.STRATEGIC, "الردع النفسي"),
                            SuggestedAction("التظاهر بأنها صدفة مخيفة والبكاء لتقليل الشك", ActionType.CHILD_FACADE, "إخفاء العبقرية الفيزيائية")
                        )
                    )
                }
            }
        }

        // 2. Later Turns and Scientific / Magic Exploration
        if (lowerText.contains("سحر") || lowerText.contains("مورفاث") || lowerText.contains("فيزياء") || actionType == ActionType.SCIENTIFIC) {
            val theorem = if (manaComprehension < 25) {
                DiscoveredTheorem(
                    id = "th_mana_conservation",
                    titleAr = "قانون حفظ المانا والديناميكا الحرارية",
                    physicsLawAr = "القانون الأول للديناميكا الحرارية (الطاقة لا تفنى ولا تستحدث من العدم)",
                    manaApplicationAr = "السحرة يضيعون 90% من طاقتهم بالصلوات الطويلة؛ ليو يستطيع توجيه تدفق المانا بكفاءة تفوق كبار السحرة بعشرة أضعاف!",
                    discoveryTurn = turnCount
                )
            } else null

            return GameEngineTurnResponse(
                narrativeAr = "تقف أمام واجهة متجر الرونيات الخشبي التابع للأكاديمية. من خلال الزجاج المتجمد، تشاهد الساحر مورفاث يصرخ محبطاً أمام دائرة سحرية مشوهة تنطفئ مراراً. عقلك الهندسي يحلل المشهد فوراً: الدائرة تعاني من هبوط في الجهد ومقاومة تدفق غير متماثلة في رسم الزوايا الحادة! إنها دائرة تيار مغناطيسي تعامل كطلاسم دينية بدائية.",
                npcDialogue = NpcDialogue(
                    speakerName = "الساحر مورفاث",
                    speechAr = "اللعنة على هذه الرونية الملعونة! لماذا تتبدد مانا الجليد قبل اكتمال التكثيف؟! هل عاقبتني آلهة النور اليوم؟!",
                    tone = NpcTone.THREATENING
                ),
                energyDelta = -4,
                hungerDelta = 6,
                warmthDelta = -3,
                suspicionDelta = 6,
                childMaskDelta = 0,
                copperDelta = 0,
                manaInsightDelta = 20,
                reputationDelta = 5,
                suggestedActions = listOf(
                    SuggestedAction("استخدام الطبشور لرسم مسار تدفق سلس على عتبة المتجر ومغادرته", ActionType.SCIENTIFIC, "إثبات القانون دون كشف هويتك"),
                    SuggestedAction("طرق الباب والتظاهر بالجوع مع رمي تلميح ذكي عن زاوية الرونية", ActionType.STRATEGIC, "كسب طعام ومأوى لدى الساحر"),
                    SuggestedAction("الطلب ببراءة: 'يا عمي الساحر، لماذا الخط يشبه حرف الـ S؟'", ActionType.CHILD_FACADE, "توجيهه بذكاء خفي")
                ),
                newTheorem = theorem
            )
        }

        // 3. Market / Food / Street Survival
        if (lowerText.contains("طعام") || lowerText.contains("سوق") || lowerText.contains("شراء") || lowerText.contains("خبز")) {
            val hasMoney = copperCoins >= 2
            return if (hasMoney) {
                GameEngineTurnResponse(
                    narrativeAr = "تصل إلى كشك الخباز في ساحة السوق. تدفع قطعتين نحاسيتين وتشتري رغيف خبز شعير دافئاً. عندما تبتلع اللقمة الأولى تشعر بالدفء ينتشر في معدتك المتآكلة من الجوع، وترتفع معنوياتك وطاقتك الحيوية بشكل ملموس.",
                    npcDialogue = NpcDialogue(
                        speakerName = "خباز السوق",
                        speechAr = "خذ يا صغيري وابتعد عن واجهة المحل... لا تدع زبائن النبلاء يرون شحاذاً هنا.",
                        tone = NpcTone.INDIFFERENT
                    ),
                    energyDelta = 25,
                    hungerDelta = -40,
                    warmthDelta = 20,
                    suspicionDelta = -2,
                    childMaskDelta = 5,
                    copperDelta = -2,
                    manaInsightDelta = 2,
                    reputationDelta = 3,
                    suggestedActions = listOf(
                        SuggestedAction("البحث عن مكان دافئ في السوق لدراسة حركة الهواء الساخن", ActionType.SCIENTIFIC, "تطوير حماية حرارية ذاتية"),
                        SuggestedAction("مراقبة تاجر الملح وحساب حجم أكياسه لكشف الغش", ActionType.STRATEGIC, "ابتزاز التاجر الغشاش لكسب أموال"),
                        SuggestedAction("العودة للأزقة ومساعدة أطفال الشوارع لنيل ولائهم", ActionType.DARING, "بناء عصابة استخبارات خاصة بك")
                    )
                )
            } else {
                GameEngineTurnResponse(
                    narrativeAr = "رائحة الحساء والخبز الساخن تعذب حواسك في السوق، لكن جيوبك فارغة من النحاس. ينظر إليك الباعة باشمئزاز ويهدد أحدهم بضربك بعصاه إذا اقتربت من السلع. يتطلب البقاء إما استغلال ذكائك للتفاوض أو المغامرة بحيلة جريئة.",
                    npcDialogue = NpcDialogue(
                        speakerName = "بائع الحساء",
                        speechAr = "ابتعد أيها المتشرد القذر! هذا الحساء لأصحاب الفضة والنحاس فقط!",
                        tone = NpcTone.THREATENING
                    ),
                    energyDelta = -6,
                    hungerDelta = 10,
                    warmthDelta = -5,
                    suspicionDelta = 0,
                    childMaskDelta = 0,
                    copperDelta = 0,
                    manaInsightDelta = 0,
                    reputationDelta = 0,
                    suggestedActions = listOf(
                        SuggestedAction("حساب ميزان تاجر الحبوب وإثبات أن كفته غير متوازنة أمام الناس", ActionType.STRATEGIC, "كسب مكافأة أو إسكات من التاجر"),
                        SuggestedAction("التظاهر بالإغماء من الجوع أمام سيدة نبيلة كريمة", ActionType.CHILD_FACADE, "استدرار الشفقة لكسب طعام"),
                        SuggestedAction("البحث في براميل القمامة خلف مطبخ نزل الفرسان", ActionType.DARING, "إيجاد طعام طارئ دون تكلفة")
                    )
                )
            }
        }

        // 4. Default Procedural Turn
        return GameEngineTurnResponse(
            narrativeAr = "تخطو في دروب إلدوريا المغطاة بالثلوج القاسية. عقلك التحليلي لا يتوقف عن حساب تدفق الهواء، وحرارة الأجسام، وحركة الحراس ونقاط الضعف في هذا النظام الإقطاعي المتسلط. كل خطوة محسوبة بعناية بين البقاء كطفل ضعيف والتصرف كعقل فذ يقود إمبراطورية من الظل.",
            npcDialogue = NpcDialogue(
                speakerName = "صوت المدينة",
                speechAr = "أجراس المعبد تدق في الأفق معلنة اقتراب وقت التفتيش الديني للدوريات...",
                tone = NpcTone.INDIFFERENT
            ),
            energyDelta = -5,
            hungerDelta = 5,
            warmthDelta = -4,
            suspicionDelta = 2,
            childMaskDelta = 0,
            copperDelta = 1,
            manaInsightDelta = 5,
            reputationDelta = 2,
            suggestedActions = listOf(
                SuggestedAction("مراقبة دورية فرسان المعبد وتحليل نقاط ضعف دروعهم", ActionType.STRATEGIC, "التخطيط لأي مواجهة مستقبلية"),
                SuggestedAction("التدرب على تفريغ المانا في قطعة الطبشور لصنع خاتم حماية", ActionType.SCIENTIFIC, "تطبيق الفيزياء في بناء تعاويذ"),
                SuggestedAction("التظاهر باللعب بكرات الثلج لإخفاء التفكير العميق", ActionType.CHILD_FACADE, "تشتيت انظار المراقبين والرهبان")
            )
        )
    }
}
