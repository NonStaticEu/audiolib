/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.wave;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * <a href="https://github.com/tpn/winsdk-10/blob/master/Include/10.0.14393.0/shared/mmreg.h">...</a>
*/
public enum WaveFormat {

  UNKNOWN                    ((short) 0x0000), // Microsoft Corporation
  PCM                        ((short) 0x0001), // Pulse Code Modulation
  ADPCM                      ((short) 0x0002), // Microsoft Corporation
  IEEE_FLOAT                 ((short) 0x0003), // Microsoft Corporation
  VSELP                      ((short) 0x0004), // Compaq Computer Corp.
  IBM_CVSD                   ((short) 0x0005), // IBM Corporation
  ALAW                       ((short) 0x0006), // Microsoft Corporation
  MULAW                      ((short) 0x0007), // Microsoft Corporation
  DTS                        ((short) 0x0008), // Microsoft Corporation
  DRM                        ((short) 0x0009), // Microsoft Corporation
  WMAVOICE9                  ((short) 0x000A), // Microsoft Corporation
  WMAVOICE10                 ((short) 0x000B), // Microsoft Corporation
  OKI_ADPCM                  ((short) 0x0010), // OKI
  DVI_ADPCM                  ((short) 0x0011), // Intel Corporation
  IMA_ADPCM                  ((short) 0x0011), // Intel Corporation
  MEDIASPACE_ADPCM           ((short) 0x0012), // Videologic
  SIERRA_ADPCM               ((short) 0x0013), // Sierra Semiconductor Corp
  G723_ADPCM                 ((short) 0x0014), // Antex Electronics Corporation
  DIGISTD                    ((short) 0x0015), // DSP Solutions, Inc.
  DIGIFIX                    ((short) 0x0016), // DSP Solutions, Inc.
  DIALOGIC_OKI_ADPCM         ((short) 0x0017), // Dialogic Corporation
  MEDIAVISION_ADPCM          ((short) 0x0018), // Media Vision, Inc.
  CU_CODEC                   ((short) 0x0019), // Hewlett-Packard Company
  HP_DYN_VOICE               ((short) 0x001A), // Hewlett-Packard Company
  YAMAHA_ADPCM               ((short) 0x0020), // Yamaha Corporation of America
  SONARC                     ((short) 0x0021), // Speech Compression
  DSPGROUP_TRUESPEECH        ((short) 0x0022), // DSP Group, Inc
  ECHOSC1                    ((short) 0x0023), // Echo Speech Corporation
  AUDIOFILE_AF36             ((short) 0x0024), // Virtual Music, Inc.
  APTX                       ((short) 0x0025), // Audio Processing Technology
  AUDIOFILE_AF10             ((short) 0x0026), // Virtual Music, Inc.
  PROSODY_1612               ((short) 0x0027), // Aculab plc
  LRC                        ((short) 0x0028), // Merging Technologies S.A.
  DOLBY_AC2                  ((short) 0x0030), // Dolby Laboratories
  GSM610                     ((short) 0x0031), // Microsoft Corporation
  MSNAUDIO                   ((short) 0x0032), // Microsoft Corporation
  ANTEX_ADPCME               ((short) 0x0033), // Antex Electronics Corporation
  CONTROL_RES_VQLPC          ((short) 0x0034), // Control Resources Limited
  DIGIREAL                   ((short) 0x0035), // DSP Solutions, Inc.
  DIGIADPCM                  ((short) 0x0036), // DSP Solutions, Inc.
  CONTROL_RES_CR10           ((short) 0x0037), // Control Resources Limited
  NMS_VBXADPCM               ((short) 0x0038), // Natural MicroSystems
  CS_IMAADPCM                ((short) 0x0039), // Crystal Semiconductor IMA ADPCM
  ECHOSC3                    ((short) 0x003A), // Echo Speech Corporation
  ROCKWELL_ADPCM             ((short) 0x003B), // Rockwell International
  ROCKWELL_DIGITALK          ((short) 0x003C), // Rockwell International
  XEBEC                      ((short) 0x003D), // Xebec Multimedia Solutions Limited
  G721_ADPCM                 ((short) 0x0040), // Antex Electronics Corporation
  G728_CELP                  ((short) 0x0041), // Antex Electronics Corporation
  MSG723                     ((short) 0x0042), // Microsoft Corporation
  INTEL_G723_1               ((short) 0x0043), // Intel Corp.
  INTEL_G729                 ((short) 0x0044), // Intel Corp.
  SHARP_G726                 ((short) 0x0045), // Sharp
  MPEG                       ((short) 0x0050), // Microsoft Corporation
  RT24                       ((short) 0x0052), // InSoft, Inc.
  PAC                        ((short) 0x0053), // InSoft, Inc.
  MPEGLAYER3                 ((short) 0x0055), // ISO/MPEG Layer3 Format Tag
  LUCENT_G723                ((short) 0x0059), // Lucent Technologies
  CIRRUS                     ((short) 0x0060), // Cirrus Logic
  ESPCM                      ((short) 0x0061), // ESS Technology
  VOXWARE                    ((short) 0x0062), // Voxware Inc
  CANOPUS_ATRAC              ((short) 0x0063), // Canopus, co., Ltd.
  G726_ADPCM                 ((short) 0x0064), // APICOM
  G722_ADPCM                 ((short) 0x0065), // APICOM
  DSAT                       ((short) 0x0066), // Microsoft Corporation
  DSAT_DISPLAY               ((short) 0x0067), // Microsoft Corporation
  VOXWARE_BYTE_ALIGNED       ((short) 0x0069), // Voxware Inc
  VOXWARE_AC8                ((short) 0x0070), // Voxware Inc
  VOXWARE_AC10               ((short) 0x0071), // Voxware Inc
  VOXWARE_AC16               ((short) 0x0072), // Voxware Inc
  VOXWARE_AC20               ((short) 0x0073), // Voxware Inc
  VOXWARE_RT24               ((short) 0x0074), // Voxware Inc
  VOXWARE_RT29               ((short) 0x0075), // Voxware Inc
  VOXWARE_RT29HW             ((short) 0x0076), // Voxware Inc
  VOXWARE_VR12               ((short) 0x0077), // Voxware Inc
  VOXWARE_VR18               ((short) 0x0078), // Voxware Inc
  VOXWARE_TQ40               ((short) 0x0079), // Voxware Inc
  VOXWARE_SC3                ((short) 0x007A), // Voxware Inc
  VOXWARE_SC3_1              ((short) 0x007B), // Voxware Inc
  SOFTSOUND                  ((short) 0x0080), // Softsound, Ltd.
  VOXWARE_TQ60               ((short) 0x0081), // Voxware Inc
  MSRT24                     ((short) 0x0082), // Microsoft Corporation
  G729A                      ((short) 0x0083), // AT&T Labs, Inc.
  MVI_MVI2                   ((short) 0x0084), // Motion Pixels
  DF_G726                    ((short) 0x0085), // DataFusion Systems (Pty) (Ltd)
  DF_GSM610                  ((short) 0x0086), // DataFusion Systems (Pty) (Ltd)
  ISIAUDIO                   ((short) 0x0088), // Iterated Systems, Inc.
  ONLIVE                     ((short) 0x0089), // OnLive! Technologies, Inc.
  MULTITUDE_FT_SX20          ((short) 0x008A), // Multitude Inc.
  INFOCOM_ITS_G721_ADPCM     ((short) 0x008B), // Infocom
  CONVEDIA_G729              ((short) 0x008C), // Convedia Corp.
  CONGRUENCY                 ((short) 0x008D), // Congruency Inc.
  SBC24                      ((short) 0x0091), // Siemens Business Communications Sys
  DOLBY_AC3_SPDIF            ((short) 0x0092), // Sonic Foundry
  MEDIASONIC_G723            ((short) 0x0093), // MediaSonic
  PROSODY_8KBPS              ((short) 0x0094), // Aculab plc
  ZYXEL_ADPCM                ((short) 0x0097), // ZyXEL Communications, Inc.
  PHILIPS_LPCBB              ((short) 0x0098), // Philips Speech Processing
  PACKED                     ((short) 0x0099), // Studer Professional Audio AG
  MALDEN_PHONYTALK           ((short) 0x00A0), // Malden Electronics Ltd.
  RACAL_RECORDER_GSM         ((short) 0x00A1), // Racal recorders
  RACAL_RECORDER_G720_A      ((short) 0x00A2), // Racal recorders
  RACAL_RECORDER_G723_1      ((short) 0x00A3), // Racal recorders
  RACAL_RECORDER_TETRA_ACELP ((short) 0x00A4), // Racal recorders
  NEC_AAC                    ((short) 0x00B0), // NEC Corp.
  RAW_AAC1                   ((short) 0x00FF), // For Raw AAC, with format block AudioSpecificConfig() (as defined by MPEG-4), that follows WAVEFORMATEX
  RHETOREX_ADPCM             ((short) 0x0100), // Rhetorex Inc.
  IRAT                       ((short) 0x0101), // BeCubed Software Inc.
  IBM_FORMAT_MULAW           ((short) 0x0101), // IBM mu-law format
  IBM_FORMAT_ALAW            ((short) 0x0102), // IBM a-law format
  IBM_FORMAT_ADPCM           ((short) 0x0103), // IBM AVC Adaptive Differential Pulse Code Modulation format
  VIVO_G723                  ((short) 0x0111), // Vivo Software
  VIVO_SIREN                 ((short) 0x0112), // Vivo Software
  PHILIPS_CELP               ((short) 0x0120), // Philips Speech Processing
  PHILIPS_GRUNDIG            ((short) 0x0121), // Philips Speech Processing
  DIGITAL_G723               ((short) 0x0123), // Digital Equipment Corporation
  SANYO_LD_ADPCM             ((short) 0x0125), // Sanyo Electric Co., Ltd.
  SIPROLAB_ACEPLNET          ((short) 0x0130), // Sipro Lab Telecom Inc.
  SIPROLAB_ACELP4800         ((short) 0x0131), // Sipro Lab Telecom Inc.
  SIPROLAB_ACELP8V3          ((short) 0x0132), // Sipro Lab Telecom Inc.
  SIPROLAB_G729              ((short) 0x0133), // Sipro Lab Telecom Inc.
  SIPROLAB_G729A             ((short) 0x0134), // Sipro Lab Telecom Inc.
  SIPROLAB_KELVIN            ((short) 0x0135), // Sipro Lab Telecom Inc.
  VOICEAGE_AMR               ((short) 0x0136), // VoiceAge Corp.
  G726ADPCM                  ((short) 0x0140), // Dictaphone Corporation
  DICTAPHONE_CELP68          ((short) 0x0141), // Dictaphone Corporation
  DICTAPHONE_CELP54          ((short) 0x0142), // Dictaphone Corporation
  QUALCOMM_PUREVOICE         ((short) 0x0150), // Qualcomm, Inc.
  QUALCOMM_HALFRATE          ((short) 0x0151), // Qualcomm, Inc.
  TUBGSM                     ((short) 0x0155), // Ring Zero Systems, Inc.
  MSAUDIO1                   ((short) 0x0160), // Microsoft Corporation
  WMAUDIO2                   ((short) 0x0161), // Microsoft Corporation
  WMAUDIO3                   ((short) 0x0162), // Microsoft Corporation
  WMAUDIO_LOSSLESS           ((short) 0x0163), // Microsoft Corporation
  WMASPDIF                   ((short) 0x0164), // Microsoft Corporation
  UNISYS_NAP_ADPCM           ((short) 0x0170), // Unisys Corp.
  UNISYS_NAP_ULAW            ((short) 0x0171), // Unisys Corp.
  UNISYS_NAP_ALAW            ((short) 0x0172), // Unisys Corp.
  UNISYS_NAP_16K             ((short) 0x0173), // Unisys Corp.
  SYCOM_ACM_SYC008           ((short) 0x0174), // SyCom Technologies
  SYCOM_ACM_SYC701_G726L     ((short) 0x0175), // SyCom Technologies
  SYCOM_ACM_SYC701_CELP54    ((short) 0x0176), // SyCom Technologies
  SYCOM_ACM_SYC701_CELP68    ((short) 0x0177), // SyCom Technologies
  KNOWLEDGE_ADVENTURE_ADPCM  ((short) 0x0178), // Knowledge Adventure, Inc.
  FRAUNHOFER_IIS_MPEG2_AAC   ((short) 0x0180), // Fraunhofer IIS
  DTS_DS                     ((short) 0x0190), // Digital Theatre Systems, Inc.
  CREATIVE_ADPCM             ((short) 0x0200), // Creative Labs, Inc
  CREATIVE_FASTSPEECH8       ((short) 0x0202), // Creative Labs, Inc
  CREATIVE_FASTSPEECH10      ((short) 0x0203), // Creative Labs, Inc
  UHER_ADPCM                 ((short) 0x0210), // UHER informatic GmbH
  ULEAD_DV_AUDIO             ((short) 0x0215), // Ulead Systems, Inc.
  ULEAD_DV_AUDIO_1           ((short) 0x0216), // Ulead Systems, Inc.
  QUARTERDECK                ((short) 0x0220), // Quarterdeck Corporation
  ILINK_VC                   ((short) 0x0230), // I-link Worldwide
  RAW_SPORT                  ((short) 0x0240), // Aureal Semiconductor
  ESST_AC3                   ((short) 0x0241), // ESS Technology, Inc.
  GENERIC_PASSTHRU           ((short) 0x0249),
  IPI_HSX                    ((short) 0x0250), // Interactive Products, Inc.
  IPI_RPELP                  ((short) 0x0251), // Interactive Products, Inc.
  CS2                        ((short) 0x0260), // Consistent Software
  SONY_SCX                   ((short) 0x0270), // Sony Corp.
  SONY_SCY                   ((short) 0x0271), // Sony Corp.
  SONY_ATRAC3                ((short) 0x0272), // Sony Corp.
  SONY_SPC                   ((short) 0x0273), // Sony Corp.
  TELUM_AUDIO                ((short) 0x0280), // Telum Inc.
  TELUM_IA_AUDIO             ((short) 0x0281), // Telum Inc.
  NORCOM_VOICE_SYSTEMS_ADPCM ((short) 0x0285), // Norcom Electronics Corp.
  FM_TOWNS_SND               ((short) 0x0300), // Fujitsu Corp.
  MICRONAS                   ((short) 0x0350), // Micronas Semiconductors, Inc.
  MICRONAS_CELP833           ((short) 0x0351), // Micronas Semiconductors, Inc.
  BTV_DIGITAL                ((short) 0x0400), // Brooktree Corporation
  INTEL_MUSIC_CODER          ((short) 0x0401), // Intel Corp.
  INDEO_AUDIO                ((short) 0x0402), // Ligos
  QDESIGN_MUSIC              ((short) 0x0450), // QDesign Corporation
  ON2_VP7_AUDIO              ((short) 0x0500), // On2 Technologies
  ON2_VP6_AUDIO              ((short) 0x0501), // On2 Technologies
  VME_VMPCM                  ((short) 0x0680), // AT&T Labs, Inc.
  TPC                        ((short) 0x0681), // AT&T Labs, Inc.
  LIGHTWAVE_LOSSLESS         ((short) 0x08AE), // Clearjump
  OLIGSM                     ((short) 0x1000), // Ing C. Olivetti & C., S.p.A.
  OLIADPCM                   ((short) 0x1001), // Ing C. Olivetti & C., S.p.A.
  OLICELP                    ((short) 0x1002), // Ing C. Olivetti & C., S.p.A.
  OLISBC                     ((short) 0x1003), // Ing C. Olivetti & C., S.p.A.
  OLIOPR                     ((short) 0x1004), // Ing C. Olivetti & C., S.p.A.
  LH_CODEC                   ((short) 0x1100), // Lernout & Hauspie
  LH_CODEC_CELP              ((short) 0x1101), // Lernout & Hauspie
  LH_CODEC_SBC8              ((short) 0x1102), // Lernout & Hauspie
  LH_CODEC_SBC12             ((short) 0x1103), // Lernout & Hauspie
  LH_CODEC_SBC16             ((short) 0x1104), // Lernout & Hauspie
  NORRIS                     ((short) 0x1400), // Norris Communications, Inc.
  ISIAUDIO_2                 ((short) 0x1401), // ISIAudio
  SOUNDSPACE_MUSICOMPRESS    ((short) 0x1500), // AT&T Labs, Inc.
  MPEG_ADTS_AAC              ((short) 0x1600), // Microsoft Corporation
  MPEG_RAW_AAC               ((short) 0x1601), // Microsoft Corporation
  MPEG_LOAS                  ((short) 0x1602), // Microsoft Corporation (MPEG-4 Audio Transport Streams (LOAS/LATM)
  NOKIA_MPEG_ADTS_AAC        ((short) 0x1608), // Microsoft Corporation
  NOKIA_MPEG_RAW_AAC         ((short) 0x1609), // Microsoft Corporation
  VODAFONE_MPEG_ADTS_AAC     ((short) 0x160A), // Microsoft Corporation
  VODAFONE_MPEG_RAW_AAC      ((short) 0x160B), // Microsoft Corporation
  MPEG_HEAAC                 ((short) 0x1610), // Microsoft Corporation (MPEG-2 AAC or MPEG-4 HE-AAC v1/v2 streams with any payload (ADTS, ADIF, LOAS/LATM, RAW). Format block includes MP4 AudioSpecificConfig() -- see HEAACWAVEFORMAT below
  VOXWARE_RT24_SPEECH        ((short) 0x181C), // Voxware Inc.
  SONICFOUNDRY_LOSSLESS      ((short) 0x1971), // Sonic Foundry
  INNINGS_TELECOM_ADPCM      ((short) 0x1979), // Innings Telecom Inc.
  LUCENT_SX8300P             ((short) 0x1C07), // Lucent Technologies
  LUCENT_SX5363S             ((short) 0x1C0C), // Lucent Technologies
  CUSEEME                    ((short) 0x1F03), // CUSeeMe
  NTCSOFT_ALF2CM_ACM         ((short) 0x1FC4), // NTCSoft
  DVM                        ((short) 0x2000), // FAST Multimedia AG
  DTS2                       ((short) 0x2001),
  MAKEAVIS                   ((short) 0x3313),
  DIVIO_MPEG4_AAC            ((short) 0x4143), // Divio, Inc.
  NOKIA_ADAPTIVE_MULTIRATE   ((short) 0x4201), // Nokia
  DIVIO_G726                 ((short) 0x4243), // Divio, Inc.
  LEAD_SPEECH                ((short) 0x434C), // LEAD Technologies
  LEAD_VORBIS                ((short) 0x564C), // LEAD Technologies
  WAVPACK_AUDIO              ((short) 0x5756), // xiph.org
  ALAC                       ((short) 0x6C61), // Apple Lossless
  OGG_VORBIS_MODE_1          ((short) 0x674F), // Ogg Vorbis
  OGG_VORBIS_MODE_2          ((short) 0x6750), // Ogg Vorbis
  OGG_VORBIS_MODE_3          ((short) 0x6751), // Ogg Vorbis
  OGG_VORBIS_MODE_1_PLUS     ((short) 0x676F), // Ogg Vorbis
  OGG_VORBIS_MODE_2_PLUS     ((short) 0x6770), // Ogg Vorbis
  OGG_VORBIS_MODE_3_PLUS     ((short) 0x6771), // Ogg Vorbis
  _3COM_NBX                  ((short) 0x7000), // 3COM Corp.
  OPUS                       ((short) 0x704F), // Opus
  FAAD_AAC                   ((short) 0x706D),
  AMR_NB                     ((short) 0x7361), // AMR Narrowband
  AMR_WB                     ((short) 0x7362), // AMR Wideband
  AMR_WP                     ((short) 0x7363), // AMR Wideband Plus
  GSM_AMR_CBR                ((short) 0x7A21), // GSMA/3GPP
  GSM_AMR_VBR_SID            ((short) 0x7A22), // GSMA/3GPP
  COMVERSE_INFOSYS_G723_1    ((short) 0xA100), // Comverse Infosys
  COMVERSE_INFOSYS_AVQSBC    ((short) 0xA101), // Comverse Infosys
  COMVERSE_INFOSYS_SBC       ((short) 0xA102), // Comverse Infosys
  SYMBOL_G729_A              ((short) 0xA103), // Symbol Technologies
  VOICEAGE_AMR_WB            ((short) 0xA104), // VoiceAge Corp.
  INGENIENT_G726             ((short) 0xA105), // Ingenient Technologies, Inc.
  MPEG4_AAC                  ((short) 0xA106), // ISO/MPEG-4
  ENCORE_G726                ((short) 0xA107), // Encore Software
  ZOLL_ASAO                  ((short) 0xA108), // ZOLL Medical Corp.
  SPEEX_VOICE                ((short) 0xA109), // xiph.org
  VIANIX_MASC                ((short) 0xA10A), // Vianix LLC
  WM9_SPECTRUM_ANALYZER      ((short) 0xA10B), // Microsoft
  WMF_SPECTRUM_ANAYZER       ((short) 0xA10C), // Microsoft
  GSM_610                    ((short) 0xA10D),
  GSM_620                    ((short) 0xA10E),
  GSM_660                    ((short) 0xA10F),
  GSM_690                    ((short) 0xA110),
  GSM_ADAPTIVE_MULTIRATE_WB  ((short) 0xA111),
  POLYCOM_G722               ((short) 0xA112), // Polycom
  POLYCOM_G728               ((short) 0xA113), // Polycom
  POLYCOM_G729_A             ((short) 0xA114), // Polycom
  POLYCOM_SIREN              ((short) 0xA115), // Polycom
  GLOBAL_IP_ILBC             ((short) 0xA116), // Global IP
  RADIOTIME_TIME_SHIFT_RADIO ((short) 0xA117), // RadioTime
  NICE_ACA                   ((short) 0xA118), // Nice Systems
  NICE_ADPCM                 ((short) 0xA119), // Nice Systems
  VOCORD_G721                ((short) 0xA11A), // Vocord Telecom
  VOCORD_G726                ((short) 0xA11B), // Vocord Telecom
  VOCORD_G722_1              ((short) 0xA11C), // Vocord Telecom
  VOCORD_G728                ((short) 0xA11D), // Vocord Telecom
  VOCORD_G729                ((short) 0xA11E), // Vocord Telecom
  VOCORD_G729_A              ((short) 0xA11F), // Vocord Telecom
  VOCORD_G723_1              ((short) 0xA120), // Vocord Telecom
  VOCORD_LBC                 ((short) 0xA121), // Vocord Telecom
  NICE_G728                  ((short) 0xA122), // Nice Systems
  FRANCE_TELECOM_G729        ((short) 0xA123), // France Telecom
  CODIAN                     ((short) 0xA124), // CODIAN
  FLAC                       ((short) 0xF1AC), // flac.sourceforge.net
  EXTENSIBLE                 ((short) 0xFFFE), // Microsoft
  DEVELOPMENT                ((short) 0xFFFF);

  private static final Map<Short, WaveFormat> FORMATS = Arrays.stream(values())
      .collect(Collectors.toUnmodifiableMap(wf -> wf.value, Function.identity(), (wf1, wf2) -> wf1));

  @Getter
  final short value;

  WaveFormat(short value) {
    this.value = value;
  }

  public static WaveFormat ofValue(short value) {
    return FORMATS.get(value);
  }
}
