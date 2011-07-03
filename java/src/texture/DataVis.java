package texture;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import april.jmat.*;
import april.vis.*;

import magic.vis.chart.*;


public class DataVis
{
    static double[][] road = new double[][] {
        { 0.0766, 1.2479, 0.2598, 0.5118, 0.2924, -0.1244, },
        { 0.0256, 1.8421, 0.5566, 0.5159, 0.2698, -0.1246, },
        { 0.0249, 1.7804, 0.3281, 0.5624, 0.2741, -0.1335, },
        { 0.0403, 1.5423, 0.4082, 0.6187, 0.2956, -0.1433, },
        { 0.0358, 1.5694, 0.3164, 0.5833, 0.3016, -0.1419, },
        { 0.0418, 1.5475, 0.3535, 0.5584, 0.2967, -0.1367, },
        { 0.0187, 1.9342, 0.5352, 0.5716, 0.2938, -0.1361, },
        { 0.0297, 1.7491, 0.3008, 0.5256, 0.2758, -0.1249, },
        { 0.0187, 1.9335, 0.4355, 0.4974, 0.2639, -0.1210, },
        { 0.0225, 1.8142, 0.4453, 0.4909, 0.2649, -0.1207, },
        { 0.0135, 2.1308, 0.8262, 0.5659, 0.2751, -0.1332, },
        { 0.0381, 1.5953, 0.1953, 0.5788, 0.2906, -0.1383, },
        { 0.0661, 1.3466, 0.1094, 0.5549, 0.2824, -0.1331, },
        { 0.0456, 1.5277, 0.1289, 0.5305, 0.2835, -0.1329, },
        { 0.0341, 1.6133, 0.2773, 0.5972, 0.3021, -0.1440, },
        { 0.0245, 1.8099, 0.5293, 0.5576, 0.2872, -0.1372, },
        { 0.0422, 1.5611, 0.4063, 0.6157, 0.3062, -0.1461, },
        { 0.0433, 1.5788, 0.4258, 0.6175, 0.3061, -0.1465, },
        { 0.0706, 1.2746, 0.2969, 0.5928, 0.2925, -0.1428, },
        { 0.0492, 1.4154, 0.3281, 0.5770, 0.2861, -0.1338, },
        { 0.0342, 1.5986, 0.4219, 0.5435, 0.2908, -0.1280, },
        { 0.0453, 1.4906, 0.3516, 0.5989, 0.2924, -0.1364, },
        { 0.0296, 1.8213, 0.5254, 0.5684, 0.2882, -0.1396, },
        { 0.0355, 1.6632, 0.3965, 0.5524, 0.2900, -0.1385, },
        { 0.0343, 1.6360, 0.3418, 0.5810, 0.3026, -0.1416, },
        { 0.0394, 1.5819, 0.3418, 0.5967, 0.3184, -0.1457, },
        { 0.0233, 1.8189, 0.7324, 0.5735, 0.3141, -0.1411, },
        { 0.0374, 1.5626, 0.3945, 0.6015, 0.3191, -0.1460, },
        { 0.0213, 1.9301, 0.7676, 0.5789, 0.3059, -0.1396, },
        { 0.0427, 1.5216, 0.3086, 0.5739, 0.2936, -0.1414, },
        { 0.0475, 1.6395, 0.3613, 0.5309, 0.2818, -0.1345, },


        { 0.0188, 2.0109, 0.5898, 0.6576, 0.2960, -0.1479, },
        { 0.0311, 1.7391, 0.4980, 0.6114, 0.2986, -0.1488, },
        { 0.0219, 1.9437, 0.4453, 0.6458, 0.2938, -0.1508, },
        { 0.0259, 1.7866, 0.5410, 0.6846, 0.3048, -0.1522, },
        { 0.0222, 1.8263, 0.4121, 0.6633, 0.2993, -0.1529, },
        { 0.0178, 1.9398, 0.4492, 0.6876, 0.2879, -0.1521, },
        { 0.0140, 2.2176, 0.8340, 0.6402, 0.2669, -0.1519, },
        { 0.0080, 2.4034, 1.4199, 0.6974, 0.2263, -0.1487, },
        { 0.0173, 1.9779, 0.4492, 0.7920, 0.2382, -0.1608, },
        { 0.0144, 2.0486, 0.5195, 0.7698, 0.2632, -0.1559, },
        { 0.0137, 2.2124, 1.1934, 0.6247, 0.2441, -0.1393, },
        { 0.0370, 1.6768, 0.5801, 0.6846, 0.3074, -0.1488, },
        { 0.0398, 1.5267, 0.3125, 0.6622, 0.3137, -0.1520, },
        { 0.0423, 1.5250, 0.3496, 0.6636, 0.3135, -0.1512, },
        { 0.0393, 1.5986, 0.4063, 0.6568, 0.3177, -0.1507, },
        { 0.0306, 1.7082, 0.3926, 0.6432, 0.3181, -0.1494, },
        { 0.0316, 1.6747, 0.4727, 0.6256, 0.3157, -0.1480, },
        { 0.0342, 1.6252, 0.3770, 0.6036, 0.3079, -0.1487, },
        { 0.0485, 1.4728, 0.3711, 0.6027, 0.3086, -0.1489, },
        { 0.0369, 1.6351, 0.4922, 0.6160, 0.3095, -0.1497, },
        { 0.0346, 1.6849, 0.4082, 0.6031, 0.3037, -0.1470, },
        { 0.0187, 1.9938, 0.6387, 0.6348, 0.2849, -0.1464, },
        { 0.0060, 2.5131, 1.8828, 0.7758, 0.2138, -0.1576, },
        { 0.0141, 2.1401, 0.8906, 0.6405, 0.2660, -0.1439, },
        { 0.0395, 1.5715, 0.3887, 0.6444, 0.3043, -0.1462, },
        { 0.0475, 1.5330, 0.3496, 0.6517, 0.3024, -0.1461, },
        { 0.0339, 1.6643, 0.5586, 0.6628, 0.3032, -0.1465, },
        { 0.0312, 1.7914, 0.4395, 0.6575, 0.2840, -0.1489, },
        { 0.0086, 2.4133, 2.1484, 0.7293, 0.2376, -0.1508, },
        { 0.0100, 2.3266, 1.3535, 0.6868, 0.2742, -0.1538, },
        { 0.0479, 1.4504, 0.3105, 0.6522, 0.3138, -0.1540, },
        { 0.0516, 1.4214, 0.3984, 0.6319, 0.3103, -0.1543, },
        { 0.0352, 1.6022, 0.3887, 0.6202, 0.3088, -0.1525, },
        { 0.0336, 1.6124, 0.4141, 0.6141, 0.3049, -0.1504, },
        { 0.0189, 1.9366, 0.6074, 0.6175, 0.3136, -0.1449, },
        { 0.0247, 1.8667, 0.6523, 0.6358, 0.3249, -0.1450, },
        { 0.0438, 1.6301, 0.4121, 0.6102, 0.2963, -0.1499, },
        { 0.0365, 1.5982, 0.3926, 0.6300, 0.3068, -0.1538, },
        { 0.0509, 1.4615, 0.3340, 0.6471, 0.3173, -0.1546, },
        { 0.0358, 1.6386, 0.6641, 0.6104, 0.3091, -0.1516, },
        { 0.0313, 1.7101, 0.6309, 0.6239, 0.3118, -0.1496, },
        { 0.0295, 1.7343, 0.7559, 0.6179, 0.3130, -0.1467, },
        { 0.0371, 1.6332, 0.6953, 0.6114, 0.3105, -0.1521, },
        { 0.0260, 1.8064, 0.6523, 0.6329, 0.3151, -0.1528, },
        { 0.0279, 1.7690, 0.5605, 0.6324, 0.3138, -0.1519, },
        { 0.0046, 2.6523, 3.6465, 0.6734, 0.2361, -0.1453, },
        { 0.0180, 2.0076, 0.9707, 0.6671, 0.3103, -0.1453, },
        { 0.0231, 1.8567, 0.6348, 0.6525, 0.3090, -0.1492, },
        { 0.0248, 1.8375, 0.8164, 0.6940, 0.3165, -0.1434, },
        { 0.0296, 1.7635, 0.7031, 0.6605, 0.3103, -0.1440, },
        { 0.0079, 2.3851, 0.9844, 0.6518, 0.2766, -0.1421, },
        { 0.0037, 2.7463, 3.3691, 0.6262, 0.2139, -0.1368, },
        { 0.0210, 1.8543, 0.7441, 0.5671, 0.2879, -0.1408, },
        { 0.0200, 1.9022, 0.7422, 0.6049, 0.3036, -0.1473, },
        { 0.0193, 1.8946, 0.7715, 0.5678, 0.3010, -0.1412, },
        { 0.0185, 1.9629, 0.7363, 0.6009, 0.3144, -0.1427, },
        { 0.0219, 1.8320, 0.5313, 0.5929, 0.3088, -0.1434, },
        { 0.0398, 1.5326, 0.3418, 0.5996, 0.3073, -0.1487, },
        { 0.0332, 1.7136, 0.5371, 0.6044, 0.3069, -0.1490, },
        { 0.0305, 1.6976, 0.5156, 0.5983, 0.3044, -0.1473, },
        { 0.0187, 1.9616, 0.5684, 0.6314, 0.2976, -0.1472, },
        { 0.0330, 1.7205, 0.4043, 0.6032, 0.3026, -0.1467, },
        { 0.0171, 1.9993, 0.6641, 0.6482, 0.3161, -0.1503, },
        { 0.0140, 2.0815, 0.8398, 0.7284, 0.2840, -0.1524, },
        { 0.0156, 2.0137, 0.5977, 0.6870, 0.3077, -0.1493, },
        { 0.0087, 2.3338, 1.2344, 0.7549, 0.2608, -0.1543, },
        { 0.0116, 2.3016, 1.2422, 0.6332, 0.2724, -0.1486, },
        { 0.0141, 2.1065, 0.6699, 0.6710, 0.3011, -0.1509, },
        { 0.0058, 2.5088, 1.1816, 0.7357, 0.2526, -0.1598, },
        { 0.0148, 2.0198, 0.7988, 0.8483, 0.1999, -0.1752, },
        { 0.0188, 1.9254, 0.4141, 0.6187, 0.2997, -0.1422, },
        { 0.0204, 1.9678, 0.4688, 0.5929, 0.2914, -0.1404, },
        { 0.0061, 2.5278, 1.6348, 0.6300, 0.2646, -0.1411, },
        { 0.0263, 1.7576, 0.5703, 0.6681, 0.3294, -0.1503, },
        { 0.0476, 1.4473, 0.3301, 0.6491, 0.3142, -0.1505, },
        { 0.0407, 1.5426, 0.3926, 0.6350, 0.3035, -0.1499, },
        { 0.0512, 1.4594, 0.3613, 0.6489, 0.3081, -0.1501, },
        { 0.0302, 1.7108, 0.5137, 0.6472, 0.3224, -0.1498, },
        { 0.0230, 1.8446, 0.6660, 0.6324, 0.3229, -0.1494, },
        { 0.0213, 1.9102, 0.6465, 0.6976, 0.3078, -0.1505, },
        { 0.0083, 2.3424, 0.9219, 0.6997, 0.2778, -0.1554, },
        { 0.0153, 2.0500, 0.8535, 0.7249, 0.2834, -0.1519, },
        { 0.0130, 2.1067, 0.8203, 0.7037, 0.2998, -0.1491, },
        { 0.0170, 2.0651, 1.3105, 0.6939, 0.3225, -0.1448, },
        { 0.0156, 2.0587, 0.8262, 0.6075, 0.3170, -0.1482, },
        { 0.0061, 2.5042, 1.2656, 0.7204, 0.2556, -0.1581, },
        { 0.0072, 2.4989, 1.6074, 0.7350, 0.2300, -0.1606, },
        { 0.0041, 2.6945, 1.9375, 0.6660, 0.2474, -0.1502, },
        { 0.0108, 2.3178, 1.7402, 0.4623, 0.2126, -0.1178, },
        { 0.0180, 1.9605, 0.4473, 0.4980, 0.2606, -0.1254, },
        { 0.0275, 1.7512, 0.3047, 0.6304, 0.3043, -0.1436, },
        { 0.0390, 1.6106, 0.3145, 0.6992, 0.3191, -0.1463, },
        { 0.0083, 2.4014, 1.3711, 0.6170, 0.2772, -0.1409, },
        { 0.0498, 1.5391, 0.3867, 0.7182, 0.3168, -0.1465, },
        { 0.0122, 2.1235, 0.6172, 0.7286, 0.2874, -0.1503, },
        { 0.0131, 2.0806, 0.7207, 0.8380, 0.2083, -0.1725, },
        { 0.0153, 2.0685, 0.8320, 0.7426, 0.2733, -0.1514, },
        { 0.0148, 2.0604, 0.6289, 0.6480, 0.2987, -0.1457, },
        { 0.0110, 2.1980, 0.7520, 0.6491, 0.2899, -0.1461, },
        { 0.0469, 1.4479, 0.3906, 0.6398, 0.3055, -0.1500, },
        { 0.0146, 2.1824, 0.8066, 0.6368, 0.2778, -0.1486, },
        { 0.0312, 1.6922, 0.4746, 0.6092, 0.2882, -0.1467, },
        { 0.0186, 1.9215, 0.4668, 0.6518, 0.3045, -0.1486, },
        { 0.0173, 2.0202, 0.5195, 0.6287, 0.3052, -0.1469, },
        { 0.0243, 1.8245, 0.5645, 0.6548, 0.3221, -0.1495, },
        { 0.0259, 1.7973, 0.7070, 0.6571, 0.3312, -0.1481, },
        { 0.0408, 1.5495, 0.4688, 0.6798, 0.3341, -0.1492, },
        { 0.0470, 1.4390, 0.3789, 0.6305, 0.2972, -0.1498, },
        { 0.0322, 1.6371, 0.5039, 0.6573, 0.3129, -0.1502, },

            { 0.0480, 1.4293, 0.1602, 0.4722, 0.2934, -0.1282, },
            { 0.0595, 1.2782, 0.2285, 0.5384, 0.3056, -0.1382, },
            { 0.1389, 0.9189, 0.0801, 0.4570, 0.2856, -0.1278, },
            { 0.0376, 1.7373, 1.0684, 0.5404, 0.3202, -0.1314, },
            { 0.0349, 1.6434, 0.4570, 0.5729, 0.3202, -0.1442, },
            { 0.0836, 1.1978, 0.1855, 0.5501, 0.3062, -0.1415, },
            { 0.0441, 1.5983, 0.5508, 0.5565, 0.2972, -0.1414, },
            { 0.0546, 1.3522, 0.3418, 0.5740, 0.3106, -0.1452, },
            { 0.0624, 1.3394, 0.3496, 0.5349, 0.3056, -0.1372, },
            { 0.0606, 1.4493, 0.4336, 0.5251, 0.2992, -0.1314, },
            { 0.0495, 1.4894, 0.4551, 0.4222, 0.2854, -0.1192, },
            { 0.0357, 1.7188, 0.5703, 0.4429, 0.2828, -0.1214, },
            { 0.0864, 1.1528, 0.1777, 0.4225, 0.2920, -0.1188, },
            { 0.0933, 1.2437, 0.6152, 0.4168, 0.2987, -0.1172, },
            { 0.0612, 1.2917, 0.2461, 0.4069, 0.2854, -0.1154, },
            { 0.0441, 1.5034, 0.2988, 0.5479, 0.3019, -0.1400, },
            { 0.0759, 1.1596, 0.2324, 0.5740, 0.3061, -0.1477, },
            { 0.0368, 1.6027, 0.4707, 0.5433, 0.3210, -0.1385, },
            { 0.0343, 1.5833, 0.2578, 0.4869, 0.2488, -0.1271, },
            { 0.0348, 1.6885, 0.3242, 0.5373, 0.2443, -0.1357, },
            { 0.0539, 1.3870, 0.2031, 0.4508, 0.2316, -0.1230, },
            { 0.0451, 1.5298, 0.1895, 0.3874, 0.2306, -0.1113, },
            { 0.0589, 1.4949, 0.2813, 0.3653, 0.2290, -0.1049, },
            { 0.0372, 1.5734, 0.4336, 0.4512, 0.2589, -0.1243, },
            { 0.0245, 1.7985, 0.5879, 0.5004, 0.2653, -0.1291, },
            { 0.0427, 1.5301, 0.3340, 0.5168, 0.2582, -0.1317, },
            { 0.0637, 1.2995, 0.2461, 0.3817, 0.2327, -0.1003, },
            { 0.0369, 1.7694, 1.8711, 0.3854, 0.2382, -0.1130, },
            { 0.0418, 1.4771, 0.4512, 0.6340, 0.3049, -0.1516, },
            { 0.0369, 1.5650, 0.4902, 0.6383, 0.3065, -0.1527, },
            { 0.0321, 1.7014, 0.6289, 0.6310, 0.3066, -0.1471, },
            { 0.0427, 1.5059, 0.3438, 0.6231, 0.2997, -0.1505, },
            { 0.0401, 1.5714, 0.4219, 0.6213, 0.2974, -0.1524, },
            { 0.0478, 1.4470, 0.3027, 0.6129, 0.2922, -0.1522, },
            { 0.0488, 1.4991, 0.4043, 0.6335, 0.2983, -0.1551, },
            { 0.0321, 1.8508, 0.5859, 0.6432, 0.2846, -0.1534, },
            { 0.0405, 1.6697, 0.5195, 0.6559, 0.2926, -0.1470, },
            { 0.0323, 1.8159, 0.4590, 0.6461, 0.2896, -0.1454, },
            { 0.0281, 1.8359, 0.5078, 0.4455, 0.2401, -0.1126, },
            { 0.0240, 1.8120, 0.3516, 0.5098, 0.2598, -0.1300, },
            { 0.0199, 1.9361, 0.3750, 0.5005, 0.2448, -0.1224, },
            { 0.0293, 1.6773, 0.2969, 0.5281, 0.2705, -0.1333, },
            { 0.0606, 1.3357, 0.2480, 0.6105, 0.2994, -0.1519, },
            { 0.0463, 1.4984, 0.3027, 0.6157, 0.3004, -0.1504, },
            { 0.0227, 1.8834, 0.4355, 0.5253, 0.2792, -0.1309, },
            { 0.0242, 1.8609, 0.6328, 0.6076, 0.3049, -0.1430, },
            { 0.0793, 1.2252, 0.1758, 0.3924, 0.2510, -0.1144, },
            { 0.0830, 1.0828, 0.1738, 0.3687, 0.2451, -0.1097, },
            { 0.1416, 0.8788, 0.0918, 0.3766, 0.2485, -0.1108, },
            { 0.1348, 0.8975, 0.1113, 0.4275, 0.2582, -0.1255, },
            { 0.0205, 2.2170, 4.9648, 0.4883, 0.2572, -0.1288, },
            { 0.1580, 0.8835, 0.1289, 0.4543, 0.2691, -0.1278, },
            { 0.1640, 0.8204, 0.1230, 0.4475, 0.2553, -0.1223, },
            { 0.0706, 1.1520, 0.2051, 0.4354, 0.2612, -0.1196, },
            { 0.0705, 1.1852, 0.2168, 0.4648, 0.2703, -0.1287, },
            { 0.1391, 0.8583, 0.1230, 0.4268, 0.2554, -0.1212, },
            { 0.1551, 0.8877, 0.1094, 0.4179, 0.2539, -0.1191, },
            { 0.0863, 1.1801, 0.2148, 0.4297, 0.2497, -0.1230, },
            { 0.0456, 1.4473, 0.2051, 0.4065, 0.2632, -0.1169, },
            { 0.0942, 1.1128, 0.2051, 0.3808, 0.2593, -0.1109, },
            { 0.0970, 1.1147, 0.1680, 0.4345, 0.2501, -0.1240, },
            { 0.0309, 1.7098, 0.5742, 0.6337, 0.3145, -0.1478, },
            { 0.0353, 1.5921, 0.4980, 0.6419, 0.3119, -0.1489, },
            { 0.0631, 1.3728, 0.3125, 0.6268, 0.3078, -0.1533, },
            { 0.0323, 1.6655, 0.4102, 0.6247, 0.3062, -0.1529, },
            { 0.0144, 2.0830, 0.4863, 0.6551, 0.2831, -0.1489, },
            { 0.0284, 1.8396, 0.6582, 0.6539, 0.3080, -0.1515, },
            { 0.0445, 1.5340, 0.4023, 0.6516, 0.3196, -0.1534, },
            { 0.0322, 1.6865, 0.6445, 0.6064, 0.3068, -0.1506, },
            { 0.0251, 1.7846, 0.6680, 0.6205, 0.3092, -0.1474, },
            { 0.0237, 1.8225, 0.7363, 0.6853, 0.3128, -0.1449, },
            { 0.0228, 1.9336, 0.3594, 0.6816, 0.2870, -0.1469, },
            { 0.0125, 2.1001, 0.5273, 0.7737, 0.2577, -0.1579, },
            { 0.0150, 2.1105, 0.6660, 0.6312, 0.2706, -0.1423, },
            { 0.0181, 2.0354, 0.7324, 0.6840, 0.3004, -0.1481, },
            { 0.0206, 1.9179, 0.5352, 0.6466, 0.2945, -0.1483, },
            { 0.0431, 1.5136, 0.3008, 0.6611, 0.3147, -0.1519, },
            { 0.0270, 1.8000, 0.4434, 0.6401, 0.3119, -0.1496, },
            { 0.0211, 1.8845, 0.6641, 0.5894, 0.3101, -0.1417, },
            { 0.0548, 1.5520, 0.2012, 0.4488, 0.2456, -0.1213, },
            { 0.0312, 1.7473, 0.2676, 0.4303, 0.2425, -0.1204, },
            { 0.0335, 1.6097, 0.4355, 0.4164, 0.2526, -0.1179, },
            { 0.0439, 1.5425, 0.3242, 0.4441, 0.2530, -0.1201, },
            { 0.0664, 1.2747, 0.1875, 0.4003, 0.2537, -0.1153, },
            { 0.0475, 1.5382, 0.3125, 0.4948, 0.2422, -0.1252, },
            { 0.0623, 1.2999, 0.2402, 0.4929, 0.2664, -0.1312, },
            { 0.0711, 1.2584, 0.2168, 0.4857, 0.2729, -0.1306, },
            { 0.0095, 2.3657, 1.9805, 0.4053, 0.2125, -0.1085, },
            { 0.0417, 1.5118, 0.5293, 0.6421, 0.3084, -0.1522, },
            { 0.0633, 1.2866, 0.2656, 0.6083, 0.2940, -0.1537, },
            { 0.0274, 1.7569, 0.3574, 0.4413, 0.2385, -0.1126, },
            { 0.0434, 1.5233, 0.4961, 0.6358, 0.3069, -0.1506, },
            { 0.0189, 1.9334, 0.3379, 0.5327, 0.2791, -0.1331, },
            { 0.0384, 1.5484, 0.2617, 0.4928, 0.2488, -0.1279, },
            { 0.0644, 1.3263, 0.2188, 0.4271, 0.2361, -0.1189, },
            { 0.0668, 1.2994, 0.2520, 0.3886, 0.2323, -0.0936, },
            { 0.0290, 1.7422, 0.4082, 0.3581, 0.2244, -0.0978, },
            { 0.0479, 1.5033, 0.3262, 0.3922, 0.2411, -0.1106, },
            { 0.0349, 1.6122, 0.2891, 0.4763, 0.2467, -0.1253, },
            { 0.0507, 1.4384, 0.3203, 0.5161, 0.2593, -0.1306, },
        };

    static double[][] other = new double[][] {
            // Scruff
            { 0.0110, 2.3759, 2.0684, 0.3316, 0.0915, -0.1000, },
            { 0.0163, 2.1703, 1.1094, 0.3329, 0.0947, -0.1003, },
            { 0.0065, 2.5421, 1.8555, 0.4313, 0.1436, -0.1252, },
            { 0.0028, 2.9461, 4.2852, 0.3715, 0.1259, -0.0980, },
            { 0.0128, 2.2114, 0.9941, 0.3117, 0.0886, -0.0958, },
            { 0.0079, 2.5160, 3.2324, 0.5021, 0.1303, -0.1252, },
            { 0.0233, 1.8935, 0.6895, 0.4293, 0.1800, -0.1109, },
            { 0.0104, 2.2678, 1.5098, 0.4903, 0.1543, -0.1176, },
            { 0.0077, 2.4025, 2.1211, 0.4744, 0.1682, -0.1187, },
            { 0.0081, 2.3972, 1.5137, 0.4790, 0.2019, -0.1189, },
            { 0.0076, 2.4032, 1.6348, 0.4681, 0.1780, -0.1151, },
            { 0.0068, 2.4974, 2.4863, 0.4368, 0.1624, -0.1100, },
            { 0.0129, 2.1534, 0.8770, 0.3685, 0.1330, -0.1071, },
            { 0.0130, 2.1150, 0.6211, 0.3821, 0.1228, -0.1077, },
            { 0.0169, 2.0100, 0.5645, 0.4039, 0.1047, -0.1183, },
            { 0.0166, 2.0229, 0.5469, 0.4133, 0.0988, -0.1206, },
            { 0.0124, 2.1389, 0.7773, 0.3982, 0.1375, -0.1077, },
            { 0.0081, 2.3979, 1.9160, 0.2940, 0.0897, -0.0855, },
            { 0.0140, 2.0844, 0.6777, 0.2213, 0.1130, -0.0524, },
            { 0.0151, 2.0709, 1.1035, 0.4075, 0.1223, -0.1017, },
            { 0.0148, 2.0762, 0.9277, 0.4096, 0.1244, -0.1019, },
            { 0.0076, 2.4103, 1.7715, 0.3991, 0.1199, -0.0998, },
            { 0.0150, 2.0868, 1.0098, 0.4078, 0.1252, -0.1034, },
            { 0.0171, 1.9951, 0.7813, 0.4068, 0.1190, -0.1028, },
            { 0.0150, 2.1072, 0.7344, 0.3741, 0.1190, -0.0868, },
            { 0.0109, 2.2883, 1.6055, 0.3831, 0.1301, -0.0893, },
            { 0.0172, 1.9783, 0.5918, 0.4227, 0.1005, -0.1195, },
            { 0.0153, 2.0315, 0.6270, 0.4325, 0.1166, -0.1176, },
            { 0.0116, 2.3518, 3.0645, 0.2473, 0.0889, -0.0653, },
            { 0.0137, 2.2730, 1.5898, 0.3457, 0.0957, -0.1029, },
            { 0.0096, 2.3218, 1.1641, 0.3098, 0.0859, -0.0969, },
            { 0.0049, 2.6712, 2.6191, 0.3830, 0.1154, -0.1121, },
            { 0.0155, 2.0599, 0.7656, 0.4536, 0.1366, -0.1166, },
            { 0.0205, 1.9159, 0.6367, 0.4339, 0.1100, -0.1184, },
            { 0.0104, 2.2918, 3.1133, 0.3084, 0.0689, -0.1031, },
            { 0.0070, 2.4418, 1.7168, 0.3674, 0.1161, -0.1123, },
            { 0.0120, 2.2569, 1.7480, 0.5182, 0.1642, -0.1369, },
            { 0.0146, 2.0455, 0.7578, 0.5194, 0.1772, -0.1420, },
            { 0.0101, 2.3243, 1.6797, 0.4184, 0.1186, -0.1279, },
            { 0.0085, 2.3684, 1.5762, 0.3401, 0.1131, -0.1036, },
            { 0.0059, 2.6090, 2.9512, 0.4357, 0.1336, -0.1051, },
            { 0.0189, 1.9000, 0.6035, 0.4294, 0.0986, -0.1224, },
            { 0.0143, 2.1241, 0.9453, 0.3487, 0.0969, -0.0984, },
            { 0.0208, 1.8911, 0.6680, 0.3477, 0.1116, -0.0986, },
            { 0.0261, 1.7829, 0.5664, 0.3610, 0.0960, -0.1020, },
            { 0.0118, 2.2481, 1.4219, 0.3309, 0.0946, -0.0917, },
            { 0.0155, 2.2322, 1.7852, 0.4294, 0.1974, -0.1148, },
            { 0.0207, 1.8729, 0.7656, 0.4603, 0.1669, -0.1088, },
            { 0.0083, 2.3530, 1.8633, 0.4413, 0.1678, -0.1085, },
            { 0.0208, 1.9282, 0.9375, 0.4722, 0.1657, -0.1067, },
            { 0.0133, 2.1719, 1.1543, 0.4522, 0.1566, -0.1019, },
            { 0.0122, 2.1748, 1.0371, 0.3806, 0.0889, -0.1114, },
            { 0.0077, 2.4461, 1.6680, 0.3129, 0.0940, -0.0829, },
            { 0.0106, 2.2435, 1.3125, 0.2539, 0.0655, -0.0689, },
            { 0.0094, 2.3127, 1.5703, 0.2892, 0.0922, -0.0765, },
            { 0.0042, 2.7409, 4.9277, 0.3678, 0.1150, -0.0873, },
            { 0.0039, 2.7759, 4.3965, 0.4207, 0.1527, -0.1033, },
            { 0.0093, 2.3265, 1.7422, 0.3242, 0.1197, -0.0794, },
            { 0.0193, 1.9326, 0.5781, 0.4264, 0.1152, -0.1120, },
            { 0.0054, 2.6472, 2.8672, 0.5102, 0.1501, -0.1279, },
            { 0.0119, 2.2523, 1.5352, 0.3922, 0.1204, -0.1063, },
            { 0.0081, 2.7179, 7.3184, 0.3617, 0.0975, -0.0770, },
            { 0.0182, 2.0253, 0.9941, 0.2159, 0.1029, -0.0557, },
            { 0.0300, 1.7119, 0.5566, 0.1996, 0.1112, -0.0475, },
            { 0.0246, 1.9905, 3.0625, 0.2169, 0.0972, -0.0538, },
            { 0.0032, 2.8524, 6.2539, 0.5283, 0.0880, -0.1063, },
            { 0.0051, 2.6565, 3.9922, 0.3812, 0.0713, -0.0876, },
            { 0.0045, 2.6838, 2.6797, 0.4088, 0.0696, -0.0910, },
            { 0.0027, 2.9558, 7.6367, 0.5546, 0.1211, -0.1120, },
            { 0.0188, 1.9390, 0.9902, 0.2073, 0.0997, -0.0554, },
            { 0.0273, 1.9357, 4.7910, 0.2097, 0.1058, -0.0536, },
            { 0.0138, 2.0961, 1.1387, 0.5531, 0.1714, -0.1127, },
            { 0.0134, 2.1126, 1.0742, 0.5466, 0.1706, -0.1118, },
            { 0.0093, 2.3943, 2.1777, 0.5282, 0.1805, -0.1130, },
            { 0.0160, 2.1294, 1.4414, 0.4863, 0.2014, -0.1202, },
            { 0.0211, 1.8373, 0.5293, 0.3960, 0.1050, -0.1063, },
            { 0.0157, 1.9834, 0.7676, 0.3693, 0.1197, -0.1050, },
            { 0.0159, 2.0598, 0.9648, 0.3957, 0.1223, -0.1033, },
            { 0.0217, 2.1298, 1.1816, 0.3899, 0.1093, -0.1090, },
            { 0.0087, 2.4126, 1.4219, 0.3643, 0.0956, -0.1013, },
            { 0.0108, 2.2873, 1.5156, 0.4389, 0.1447, -0.1162, },
            { 0.0048, 2.6836, 4.5605, 0.4247, 0.1465, -0.1152, },
            { 0.0161, 2.1768, 1.3691, 0.4033, 0.1215, -0.1121, },
            { 0.0088, 2.3883, 1.2852, 0.3481, 0.0976, -0.0982, },
            { 0.0155, 2.0507, 0.9648, 0.3728, 0.0966, -0.0941, },
            { 0.0087, 2.3486, 2.1270, 0.4189, 0.0886, -0.0955, },
            { 0.0078, 2.3824, 1.4004, 0.4428, 0.0475, -0.1042, },
            { 0.0097, 2.2937, 1.2773, 0.4469, 0.1347, -0.1033, },
            { 0.0059, 2.5230, 2.1738, 0.4429, 0.1363, -0.1014, },
            { 0.0098, 2.2865, 1.5371, 0.4637, 0.1315, -0.0989, },
            { 0.0106, 2.2501, 1.1719, 0.4481, 0.1258, -0.1037, },
            { 0.0055, 2.6094, 4.7520, 0.2940, 0.0831, -0.0781, },
            { 0.0055, 2.5662, 2.4668, 0.3531, 0.0611, -0.0840, },
            { 0.0024, 3.0033, 8.1074, 0.4573, 0.0880, -0.0968, },
            { 0.0057, 2.6027, 4.6406, 0.2822, 0.0684, -0.0785, },
            { 0.0195, 1.9061, 0.6055, 0.2247, 0.1239, -0.0604, },
            { 0.0099, 2.3324, 1.9336, 0.2296, 0.1045, -0.0574, },
            { 0.0154, 2.0549, 0.7148, 0.2224, 0.1168, -0.0606, },
            { 0.0102, 2.2489, 1.0332, 0.4285, 0.0826, -0.1058, },
            { 0.0194, 1.8756, 0.6270, 0.4196, 0.1172, -0.0996, },

            // Gravel
            { 0.0110, 2.3759, 2.0684, 0.3316, 0.0915, -0.1000, },
            { 0.0163, 2.1703, 1.1094, 0.3329, 0.0947, -0.1003, },
            { 0.0065, 2.5421, 1.8555, 0.4313, 0.1436, -0.1252, },
            { 0.0028, 2.9461, 4.2852, 0.3715, 0.1259, -0.0980, },
            { 0.0128, 2.2114, 0.9941, 0.3117, 0.0886, -0.0958, },
            { 0.0079, 2.5160, 3.2324, 0.5021, 0.1303, -0.1252, },
            { 0.0233, 1.8935, 0.6895, 0.4293, 0.1800, -0.1109, },
            { 0.0104, 2.2678, 1.5098, 0.4903, 0.1543, -0.1176, },
            { 0.0077, 2.4025, 2.1211, 0.4744, 0.1682, -0.1187, },
            { 0.0081, 2.3972, 1.5137, 0.4790, 0.2019, -0.1189, },
            { 0.0076, 2.4032, 1.6348, 0.4681, 0.1780, -0.1151, },
            { 0.0068, 2.4974, 2.4863, 0.4368, 0.1624, -0.1100, },
            { 0.0129, 2.1534, 0.8770, 0.3685, 0.1330, -0.1071, },
            { 0.0130, 2.1150, 0.6211, 0.3821, 0.1228, -0.1077, },
            { 0.0169, 2.0100, 0.5645, 0.4039, 0.1047, -0.1183, },
            { 0.0166, 2.0229, 0.5469, 0.4133, 0.0988, -0.1206, },
            { 0.0124, 2.1389, 0.7773, 0.3982, 0.1375, -0.1077, },
            { 0.0081, 2.3979, 1.9160, 0.2940, 0.0897, -0.0855, },
            { 0.0140, 2.0844, 0.6777, 0.2213, 0.1130, -0.0524, },
            { 0.0151, 2.0709, 1.1035, 0.4075, 0.1223, -0.1017, },
            { 0.0148, 2.0762, 0.9277, 0.4096, 0.1244, -0.1019, },
            { 0.0076, 2.4103, 1.7715, 0.3991, 0.1199, -0.0998, },
            { 0.0150, 2.0868, 1.0098, 0.4078, 0.1252, -0.1034, },
            { 0.0171, 1.9951, 0.7813, 0.4068, 0.1190, -0.1028, },
            { 0.0150, 2.1072, 0.7344, 0.3741, 0.1190, -0.0868, },
            { 0.0109, 2.2883, 1.6055, 0.3831, 0.1301, -0.0893, },
            { 0.0172, 1.9783, 0.5918, 0.4227, 0.1005, -0.1195, },
            { 0.0153, 2.0315, 0.6270, 0.4325, 0.1166, -0.1176, },
            { 0.0116, 2.3518, 3.0645, 0.2473, 0.0889, -0.0653, },
            { 0.0137, 2.2730, 1.5898, 0.3457, 0.0957, -0.1029, },
            { 0.0096, 2.3218, 1.1641, 0.3098, 0.0859, -0.0969, },
            { 0.0049, 2.6712, 2.6191, 0.3830, 0.1154, -0.1121, },
            { 0.0155, 2.0599, 0.7656, 0.4536, 0.1366, -0.1166, },
            { 0.0205, 1.9159, 0.6367, 0.4339, 0.1100, -0.1184, },
            { 0.0104, 2.2918, 3.1133, 0.3084, 0.0689, -0.1031, },
            { 0.0070, 2.4418, 1.7168, 0.3674, 0.1161, -0.1123, },
            { 0.0120, 2.2569, 1.7480, 0.5182, 0.1642, -0.1369, },
            { 0.0146, 2.0455, 0.7578, 0.5194, 0.1772, -0.1420, },
            { 0.0101, 2.3243, 1.6797, 0.4184, 0.1186, -0.1279, },
            { 0.0085, 2.3684, 1.5762, 0.3401, 0.1131, -0.1036, },
            { 0.0059, 2.6090, 2.9512, 0.4357, 0.1336, -0.1051, },
            { 0.0189, 1.9000, 0.6035, 0.4294, 0.0986, -0.1224, },
            { 0.0143, 2.1241, 0.9453, 0.3487, 0.0969, -0.0984, },
            { 0.0208, 1.8911, 0.6680, 0.3477, 0.1116, -0.0986, },
            { 0.0261, 1.7829, 0.5664, 0.3610, 0.0960, -0.1020, },
            { 0.0118, 2.2481, 1.4219, 0.3309, 0.0946, -0.0917, },
            { 0.0155, 2.2322, 1.7852, 0.4294, 0.1974, -0.1148, },
            { 0.0207, 1.8729, 0.7656, 0.4603, 0.1669, -0.1088, },
            { 0.0083, 2.3530, 1.8633, 0.4413, 0.1678, -0.1085, },
            { 0.0208, 1.9282, 0.9375, 0.4722, 0.1657, -0.1067, },
            { 0.0133, 2.1719, 1.1543, 0.4522, 0.1566, -0.1019, },
            { 0.0122, 2.1748, 1.0371, 0.3806, 0.0889, -0.1114, },
            { 0.0077, 2.4461, 1.6680, 0.3129, 0.0940, -0.0829, },
            { 0.0106, 2.2435, 1.3125, 0.2539, 0.0655, -0.0689, },
            { 0.0094, 2.3127, 1.5703, 0.2892, 0.0922, -0.0765, },
            { 0.0042, 2.7409, 4.9277, 0.3678, 0.1150, -0.0873, },
            { 0.0039, 2.7759, 4.3965, 0.4207, 0.1527, -0.1033, },
            { 0.0093, 2.3265, 1.7422, 0.3242, 0.1197, -0.0794, },
            { 0.0193, 1.9326, 0.5781, 0.4264, 0.1152, -0.1120, },
            { 0.0054, 2.6472, 2.8672, 0.5102, 0.1501, -0.1279, },
            { 0.0119, 2.2523, 1.5352, 0.3922, 0.1204, -0.1063, },
            { 0.0081, 2.7179, 7.3184, 0.3617, 0.0975, -0.0770, },
            { 0.0182, 2.0253, 0.9941, 0.2159, 0.1029, -0.0557, },
            { 0.0300, 1.7119, 0.5566, 0.1996, 0.1112, -0.0475, },
            { 0.0246, 1.9905, 3.0625, 0.2169, 0.0972, -0.0538, },
            { 0.0032, 2.8524, 6.2539, 0.5283, 0.0880, -0.1063, },
            { 0.0051, 2.6565, 3.9922, 0.3812, 0.0713, -0.0876, },
            { 0.0045, 2.6838, 2.6797, 0.4088, 0.0696, -0.0910, },
            { 0.0027, 2.9558, 7.6367, 0.5546, 0.1211, -0.1120, },
            { 0.0188, 1.9390, 0.9902, 0.2073, 0.0997, -0.0554, },
            { 0.0273, 1.9357, 4.7910, 0.2097, 0.1058, -0.0536, },
            { 0.0138, 2.0961, 1.1387, 0.5531, 0.1714, -0.1127, },
            { 0.0134, 2.1126, 1.0742, 0.5466, 0.1706, -0.1118, },
            { 0.0093, 2.3943, 2.1777, 0.5282, 0.1805, -0.1130, },
            { 0.0160, 2.1294, 1.4414, 0.4863, 0.2014, -0.1202, },
            { 0.0211, 1.8373, 0.5293, 0.3960, 0.1050, -0.1063, },
            { 0.0157, 1.9834, 0.7676, 0.3693, 0.1197, -0.1050, },
            { 0.0159, 2.0598, 0.9648, 0.3957, 0.1223, -0.1033, },
            { 0.0217, 2.1298, 1.1816, 0.3899, 0.1093, -0.1090, },
            { 0.0087, 2.4126, 1.4219, 0.3643, 0.0956, -0.1013, },
            { 0.0108, 2.2873, 1.5156, 0.4389, 0.1447, -0.1162, },
            { 0.0048, 2.6836, 4.5605, 0.4247, 0.1465, -0.1152, },
            { 0.0161, 2.1768, 1.3691, 0.4033, 0.1215, -0.1121, },
            { 0.0088, 2.3883, 1.2852, 0.3481, 0.0976, -0.0982, },
            { 0.0155, 2.0507, 0.9648, 0.3728, 0.0966, -0.0941, },
            { 0.0087, 2.3486, 2.1270, 0.4189, 0.0886, -0.0955, },
            { 0.0078, 2.3824, 1.4004, 0.4428, 0.0475, -0.1042, },
            { 0.0097, 2.2937, 1.2773, 0.4469, 0.1347, -0.1033, },
            { 0.0059, 2.5230, 2.1738, 0.4429, 0.1363, -0.1014, },
            { 0.0098, 2.2865, 1.5371, 0.4637, 0.1315, -0.0989, },
            { 0.0106, 2.2501, 1.1719, 0.4481, 0.1258, -0.1037, },
            { 0.0055, 2.6094, 4.7520, 0.2940, 0.0831, -0.0781, },
            { 0.0055, 2.5662, 2.4668, 0.3531, 0.0611, -0.0840, },
            { 0.0024, 3.0033, 8.1074, 0.4573, 0.0880, -0.0968, },
            { 0.0057, 2.6027, 4.6406, 0.2822, 0.0684, -0.0785, },
            { 0.0195, 1.9061, 0.6055, 0.2247, 0.1239, -0.0604, },
            { 0.0099, 2.3324, 1.9336, 0.2296, 0.1045, -0.0574, },
            { 0.0154, 2.0549, 0.7148, 0.2224, 0.1168, -0.0606, },
            { 0.0102, 2.2489, 1.0332, 0.4285, 0.0826, -0.1058, },
            { 0.0194, 1.8756, 0.6270, 0.4196, 0.1172, -0.0996, },
            { 0.0047, 2.7429, 6.0488, 0.6298, 0.2476, -0.1583, },
            { 0.0055, 2.5411, 3.1660, 0.5624, 0.2617, -0.1528, },
            { 0.0046, 2.7357, 4.2617, 0.6522, 0.2378, -0.1679, },
            { 0.0046, 2.6472, 3.5137, 0.5889, 0.2137, -0.1561, },
            { 0.0056, 2.5841, 3.5449, 0.6003, 0.2343, -0.1567, },
            { 0.0051, 2.6488, 2.9453, 0.4908, 0.2330, -0.1395, },
            { 0.0073, 2.4697, 3.1758, 0.5684, 0.2495, -0.1527, },
            { 0.0070, 2.4954, 3.9941, 0.5510, 0.2365, -0.1461, },
            { 0.0057, 2.5211, 2.7598, 0.6114, 0.2328, -0.1606, },
            { 0.0090, 2.3387, 1.6230, 0.3386, 0.1900, -0.1067, },
            { 0.0112, 2.2158, 1.5059, 0.2728, 0.1693, -0.0857, },
            { 0.0096, 2.2722, 1.6348, 0.2844, 0.1858, -0.0889, },
            { 0.0095, 2.2799, 1.4824, 0.3267, 0.1767, -0.1028, },
            { 0.0103, 2.2719, 1.4746, 0.3367, 0.1824, -0.1048, },
            { 0.0092, 2.3833, 3.1426, 0.3758, 0.2050, -0.1126, },
            { 0.0121, 2.1652, 1.3320, 0.2953, 0.1732, -0.0899, },
            { 0.0124, 2.1562, 1.3672, 0.2540, 0.1573, -0.0753, },
            { 0.0105, 2.2125, 1.2441, 0.2331, 0.1488, -0.0717, },
            { 0.0081, 2.3750, 1.6250, 0.3175, 0.1823, -0.0966, },
            { 0.0100, 2.2964, 1.6484, 0.2791, 0.1769, -0.0828, },
            { 0.0067, 2.4766, 1.8047, 0.3439, 0.1896, -0.1063, },
            { 0.0045, 2.7076, 5.1309, 0.5877, 0.2495, -0.1576, },
            { 0.0070, 2.4579, 2.9434, 0.6358, 0.2370, -0.1648, },
            { 0.0070, 2.4847, 2.1543, 0.5393, 0.2547, -0.1488, },
            { 0.0052, 2.7950, 8.3867, 0.6369, 0.2373, -0.1579, },
            { 0.0085, 2.3704, 1.8164, 0.5344, 0.2507, -0.1398, },
            { 0.0040, 2.7681, 4.9238, 0.5690, 0.2335, -0.1514, },
            { 0.0060, 2.5432, 2.6445, 0.4374, 0.2266, -0.1334, },
            { 0.0058, 2.5598, 2.7656, 0.5443, 0.2295, -0.1447, },
            { 0.0029, 2.9092, 6.6387, 0.6109, 0.2453, -0.1465, },
            { 0.0043, 2.6938, 3.2676, 0.4143, 0.1731, -0.1179, },
            { 0.0050, 2.6012, 2.9824, 0.4356, 0.2260, -0.1240, },
            { 0.0058, 2.5631, 4.2988, 0.4534, 0.1842, -0.1175, },
            { 0.0051, 2.7708, 11.6133, 0.6069, 0.1401, -0.1130, },
            { 0.0034, 2.8434, 4.8711, 0.4484, 0.2330, -0.1265, },
            { 0.0047, 2.6903, 3.4883, 0.5328, 0.2280, -0.1394, },
            { 0.0080, 2.3857, 1.7852, 0.5078, 0.2193, -0.1212, },
            { 0.0070, 2.5245, 4.4941, 0.5187, 0.1945, -0.1343, },
            { 0.0029, 2.8759, 6.8770, 0.5567, 0.2359, -0.1363, },
            { 0.0061, 2.5625, 3.4121, 0.4862, 0.2533, -0.1298, },
            { 0.0077, 2.4753, 2.5605, 0.4639, 0.2456, -0.1313, },
            { 0.0073, 2.4365, 2.2070, 0.4392, 0.2215, -0.1288, },
            { 0.0058, 2.6081, 5.4023, 0.4697, 0.2143, -0.1296, },
            { 0.0063, 2.5119, 2.6934, 0.4098, 0.2098, -0.1216, },
            { 0.0069, 2.4309, 2.0684, 0.4081, 0.2227, -0.1236, },
            { 0.0106, 2.2256, 1.8809, 0.5287, 0.2156, -0.1492, },
            { 0.0085, 2.3702, 2.1777, 0.5083, 0.2197, -0.1444, },
            { 0.0032, 2.8465, 4.5742, 0.4842, 0.1872, -0.1324, },
            { 0.0034, 2.8456, 5.4844, 0.5144, 0.1972, -0.1394, },
            { 0.0040, 2.7611, 4.2656, 0.4986, 0.2069, -0.1381, },
            { 0.0030, 2.8879, 5.3066, 0.4907, 0.2222, -0.1381, },
            { 0.0131, 2.2136, 1.5469, 0.4737, 0.1431, -0.1135, },
            { 0.0036, 2.7916, 5.2520, 0.5601, 0.1797, -0.1249, },
            { 0.0028, 2.9120, 6.9512, 0.5279, 0.1719, -0.1253, },
            { 0.0037, 2.8312, 5.4277, 0.5202, 0.1441, -0.1294, },
            { 0.0040, 2.7138, 4.1133, 0.5575, 0.1606, -0.1373, },
            { 0.0051, 2.7092, 4.5449, 0.5359, 0.1467, -0.1327, },
            { 0.0029, 2.9106, 5.3828, 0.5427, 0.1864, -0.1325, },
            { 0.0049, 2.6459, 3.3594, 0.3873, 0.1870, -0.1136, },
            { 0.0045, 2.7228, 3.4805, 0.4695, 0.1887, -0.1263, },
            { 0.0054, 2.6050, 2.4570, 0.3900, 0.1542, -0.1007, },
            { 0.0107, 2.2361, 1.0586, 0.2976, 0.1339, -0.0856, },
            { 0.0027, 2.9337, 7.8906, 0.5312, 0.1483, -0.1277, },
            { 0.0033, 2.8541, 6.0117, 0.5207, 0.2416, -0.1407, },
            { 0.0034, 2.8632, 5.2578, 0.5017, 0.2492, -0.1361, },
            { 0.0069, 2.4755, 2.3086, 0.4797, 0.2267, -0.1244, },
            { 0.0084, 2.5638, 3.6387, 0.3857, 0.2128, -0.1038, },
            { 0.0054, 2.5979, 3.3887, 0.4752, 0.2195, -0.1252, },
            { 0.0044, 2.6862, 3.9375, 0.4478, 0.2199, -0.1226, },
            { 0.0039, 2.7695, 3.7813, 0.4388, 0.2357, -0.1262, },
            { 0.0051, 2.5946, 2.4121, 0.4056, 0.2225, -0.1143, },
            { 0.0040, 2.7263, 4.4785, 0.4526, 0.2190, -0.1240, },
            { 0.0042, 2.6965, 3.7539, 0.4546, 0.2262, -0.1241, },
            { 0.0027, 2.9950, 7.6895, 0.5324, 0.2339, -0.1344, },
            { 0.0064, 2.4764, 2.5234, 0.5695, 0.2693, -0.1537, },
            { 0.0036, 2.8208, 4.8242, 0.6006, 0.2508, -0.1548, },
            { 0.0047, 2.6574, 3.4844, 0.4611, 0.2177, -0.1404, },
            { 0.0058, 2.5741, 3.6895, 0.5673, 0.2430, -0.1517, },
            { 0.0074, 2.4241, 2.5664, 0.5527, 0.2504, -0.1488, },
            { 0.0116, 2.2467, 1.5879, 0.5730, 0.2439, -0.1474, },
            { 0.0063, 2.5088, 3.0039, 0.5694, 0.2589, -0.1539, },
            { 0.0048, 2.6486, 3.4199, 0.6171, 0.2332, -0.1623, },
            { 0.0055, 2.5959, 3.7988, 0.6548, 0.2400, -0.1680, },
            { 0.0045, 2.6674, 3.8164, 0.5698, 0.2600, -0.1540, },
    };

    public static void main(String[] args)
    {
        JFrame jf = new JFrame();
        jf.setLayout(new BorderLayout());

        VisCanvas vc = new VisCanvas(new VisWorld());
        vc.setBackground(Color.black);
        jf.add(vc, BorderLayout.CENTER);

        List<double[]> roadList = Arrays.asList(road);
        List<double[]> otherList = Arrays.asList(other);

        DataNormalizer dn = new DataNormalizer();
        dn.observeLimits(roadList);
        dn.observeLimits(otherList);

        roadList = dn.normalize(roadList);
        otherList = dn.normalize(otherList);

        for (double[] d : roadList) {
            printLibSvm(d, "+1");
        }

        for (double[] d : otherList) {
            printLibSvm(d, "-1");
        }

        System.out.println("\nsvm-train -t 2 -w+1 10  -w-1 1 tools/texture.data tools/texture.model");
        printArray(dn.getLowerBounds());
        printArray(dn.getUpperBounds());

        ArrayList<double[]> pos = new ArrayList<double[]>();
        for (double[] d : roadList) {
            double[] v = new double[] {d[4], d[3], d[1]};
            pos.add(v);
        }

        ArrayList<double[]> neg = new ArrayList<double[]>();
        for (double[] d : otherList) {
            double[] v = new double[] {d[4], d[3], d[1]};
            neg.add(v);
        }

        VisPlot plot = new VisPlot();
        plot.addData(new XYDataSeries(pos, new VisDataPointStyle(Color.green, 3)));
        plot.addData(new XYDataSeries(neg, new VisDataPointStyle(Color.red, 3)));

        final VisWorld.Buffer vb = vc.getWorld().getBuffer("content");
        vb.addBuffered(plot);
        vb.switchBuffer();

        jf.setSize(800, 800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }

    static void printLibSvm(double[] d, String label)
    {
        System.out.print(label+ " ");
        for (int i=0; i<d.length; ++i) {
            System.out.printf("%d:%.4f ", i+1, d[i]);
        }
        System.out.println();
    }

    static void printArray(double[] d)
    {
        System.out.print("{ ");
        for (int i=0; i<d.length; ++i) {
            System.out.printf("%.4f, ", d[i]);
        }
        System.out.println("},");
    }
}